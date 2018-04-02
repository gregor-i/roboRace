package controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Source}
import gameLogic.EventLog
import gameLogic.gameUpdate.Command
import gameLogic.processor.Processor
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
class GameController @Inject()(repo: GameRepository)
                              (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe {

  private[this] val (sink, source) =
    MergeHub.source[String](perProducerBufferSize = 16)
      .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
      .run()

  def state(id: String) = Action{
    Ok(repo.get(id).asJson)
  }

  def sendCommand(id: String) = Action(circe.tolerantJson[Command]) { request =>
    repo.get(id) match{
      case Some(gameState) =>
        val logged = Processor(gameState)(Seq(request.body))
        repo.save(id, logged.state)
        Source[EventLog](logged.events.to[scala.collection.immutable.Iterable]).map(_.asJson.noSpaces).runWith(sink)
        Ok(logged.state.asJson)
      case None => NotFound
    }
  }

  def sse(id: String) = Action {
    Ok.chunked(source via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }
}
