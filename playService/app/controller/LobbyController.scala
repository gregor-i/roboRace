package controller

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import gameLogic.GameNotDefined
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.Inject
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.GameRepository

import scala.concurrent.ExecutionContext

class LobbyController @Inject()(repo: GameRepository)
                               (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe {

  def list() = Action {
    Ok(repo.list().toMap.asJson)
  }

  def create() = Action {
    val id = UUID.randomUUID().toString
    repo.save(id, GameNotDefined)
    Ok(Json.fromString(id))
  }

  def delete(id: String) = Action {
    repo.delete(id)
    NoContent
  }
}
