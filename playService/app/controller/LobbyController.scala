package controller

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gameLogic.{GameFinished, GameRunning, GameStarting, GameState, InitialGame}
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.Inject
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.GameRepository

import scala.concurrent.ExecutionContext

class LobbyController @Inject()(gameRepo: GameRepository)
                               (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe {

  private val (sink, source) = SinkSourceCache.createPair()

  def stateDescription(gameState:GameState): String = gameState match{
    case InitialGame => "New"
    case GameStarting(sc, pls) => s"Starting(players = ${pls.map(_.name).mkString(", ")})"
    case GameRunning(cycle, cs, pls) => s"Running(cycle = $cycle, players = ${pls.map(_.name).mkString(", ")})"
    case GameFinished(pls, sc) => s"Finished(players = ${pls.sortBy(_.finished.get.rank).map(pl => s"${pl.finished.get.rank}. ${pl.name}").mkString(", ")})"
  }

  def list() = Action {
    Ok(gameRepo.list().toMap.mapValues(stateDescription).asJson)
  }

  def create() = Action {
    val id = UUID.randomUUID().toString.take(8)
    val gameState = InitialGame
    gameRepo.save(id, gameState)
    Source.single((GameCreated(id, stateDescription(gameState)):LobbyEvent).asJson.noSpaces).runWith(sink)
    NoContent
  }

  def delete(id: String) = Action {
    gameRepo.delete(id)
    Source.single((GameDeleted(id):LobbyEvent).asJson.noSpaces).runWith(sink)
    NoContent
  }

  def sse() = Action {
    Ok.chunked(source via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }
}

sealed trait LobbyEvent
case class GameDeleted(id: String) extends LobbyEvent
case class GameCreated(id: String, state: String) extends LobbyEvent