package controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gameLogic.gameUpdate._
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.GameRepository

import scala.concurrent.ExecutionContext

@Singleton
class GameController @Inject()(repo: GameRepository)
                              (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe {

  def state(id: String) = Action {
    Ok(repo.get(id).asJson)
  }

  def sendCommand(id: String) = Action(circe.tolerantJson[Command]) { request =>
    repo.get(id) match {
      case Some(gameState) =>
        val command = request.body
        Processor(gameState, command) { loggedState =>
          repo.save(id, loggedState.state)
          Source.single(loggedState.asJson.noSpaces).runWith(SinkSourceCache.sink(id))
          Ok(loggedState.asJson)
        } { rejected =>
          BadRequest(rejected.reason.asJson)
        }
      case None => NotFound
    }
  }

  def sse(id: String) = Action {
    Ok.chunked(SinkSourceCache.source(id) via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }
}