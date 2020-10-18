package controller

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import entities.Scenario
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import logic.command.CreateGame
import model.GameResponseFactory
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.{GameRepository, GameRow}

import scala.concurrent.ExecutionContext

@Singleton
class LobbyController @Inject() (sessionAction: SessionAction, gameRepo: GameRepository)(
    implicit system: ActorSystem,
    mat: Materializer,
    ex: ExecutionContext
) extends InjectedController
    with Circe
    with JsonUtil {

  private val (sink, source) = new SinkSourceCache[Seq[GameRow]].createPair()

  def list() = sessionAction { (session, _) =>
    Ok(gameList().flatMap(GameResponseFactory(_)(session)).asJson)
  }

  def create(index: Int) = sessionAction(circe.tolerantJson[Scenario]) { (session, request) =>
    CreateGame(request.body, index)(session.id) match {
      case Left(reason) =>
        BadRequest(reason.asJson)
      case Right(game) =>
        val row = GameRow(
          id = Utils.newId(),
          owner = session.id,
          game = Some(game),
          creationTime = ZonedDateTime.now()
        )
        gameRepo.save(row)
        sendStateToClients()
        Created(GameResponseFactory(row, game)(session).asJson)
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
          .map(games => games.flatMap(GameResponseFactory(_)(session)))
          .map(_.asJson.noSpaces)
          .via(EventSource.flow)
      )
      .as(ContentTypes.EVENT_STREAM)
  }

  private def gameList(): Seq[GameRow] = gameRepo.list()
}
