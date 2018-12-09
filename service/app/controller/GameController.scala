package controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gameLogic.command.{Command, CommandAccepted, CommandRejected}
import gameLogic.gameUpdate._
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.GameRepository

import scala.concurrent.ExecutionContext

@Singleton
class GameController @Inject()(sessionAction: SessionAction,
                               repo: GameRepository)
                              (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe with JsonUtil {

  def state(id: String) = Action {
    repo.get(id) match {
      case Some(row) if row.game.isDefined => Ok(row.asJson)
      case _ => NotFound
    }
  }

  def sendCommand(id: String) = sessionAction(circe.tolerantJson[Command]) { (session, request) =>
    repo.get(id) match {
      case Some(row) if row.game.isDefined =>
        val command = request.body
        command(session.id)(row.game.get) match {
          case CommandAccepted(afterCommand) =>
            val afterCycle = Cycle(afterCommand)
            repo.save(row.copy(game = Some(afterCycle)))
            if(afterCycle.events != row.game.get.events)
              Source.single(afterCycle.asJson.noSpaces)
                .runWith(SinkSourceCache.sink(id))
            Ok(afterCycle.asJson)
          case CommandRejected(reason) =>
            BadRequest(reason.asJson)
        }
      case None => NotFound
      case Some(row) if row.game.isEmpty => NotFound
    }
  }

  def sse(id: String) = Action {
    Ok.chunked(SinkSourceCache.source(id) via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }
}
