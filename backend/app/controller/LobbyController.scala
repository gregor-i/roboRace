package controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import api.WithId
import entities.{Game, Scenario}
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import logic.command.CreateGame
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.GameRepository

import scala.concurrent.ExecutionContext

@Singleton
class LobbyController @Inject() (sessionAction: SessionAction, gameRepo: GameRepository)(
    implicit system: ActorSystem,
    mat: Materializer,
    ex: ExecutionContext
) extends InjectedController
    with Circe
    with JsonUtil {

  private val (sink, source) = new SinkSourceCache[Seq[WithId[Game]]].createPair()

  def list() = sessionAction { (session, _) =>
    Ok(gameList().asJson)
  }

  def create(index: Int) = sessionAction(circe.tolerantJson[Scenario]) { (session, request) =>
    CreateGame(request.body, index)(session.id) match {
      case Left(reason) =>
        BadRequest(reason.asJson)
      case Right(game) =>
        val id    = Utils.newId()
        val owner = session.id
        gameRepo.save(id = id, owner = owner, entity = game)
        sendStateToClients()
        Created(WithId(id = id, owner = owner, entity = game).asJson)
    }
  }

  def delete(id: String) = sessionAction { (session, request) =>
    gameRepo.get(id) match {
      case None                                 => NotFound
      case Some(row) if row.owner != session.id => Unauthorized
      case Some(_) =>
        gameRepo.delete(id)
        sendStateToClients()
        NoContent
    }
  }

  def sendStateToClients() =
    Source.single(gameList()).runWith(sink)

  def sse() = sessionAction { (session, _) =>
    Ok.chunked(
        source
          .map(_.asJson.noSpaces)
          .via(EventSource.flow)
      )
      .as(ContentTypes.EVENT_STREAM)
  }

  private def gameList(): Seq[WithId[Game]] = gameRepo.list().collect(gameRepo.rowToEntity)
}
