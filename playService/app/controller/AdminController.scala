package controller

import javax.inject.Inject
import play.api.Configuration
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gameLogic.{GameFinished, GameRunning, GameStarting, GameState, InitialGame}
import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.Inject
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.GameRepository

import scala.concurrent.ExecutionContext
import com.typesafe.config.ConfigValue
import io.circe.Json.JString

class AdminController @Inject()(configuration: Configuration) extends InjectedController with Circe {
  implicit val encodeConfigVal: Encoder[ConfigValue] = Encoder.encodeString.contramap[ConfigValue](_.unwrapped.toString) //JString(a.unwrapped().toString)

//  implicit val encodeConfigVal: Encoder[ConfigValue] = new Encoder[ConfigValue] {
//    override def apply(a: ConfigValue): Json =  Encoder.encodeString.contramap()JString(a.unwrapped().toString)
//  }

  def config() = Action{
    Ok(configuration.entrySet.toMap.asJson)
  }
}
