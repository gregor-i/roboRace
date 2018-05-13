package controller

import java.util.UUID

import gameLogic.GameScenario
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.circe.Circe
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.InjectedController

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ScenarioController @Inject()(ws: WSClient)(implicit ex: ExecutionContext)
  extends InjectedController
    with Circe {

  val token = "5a9e4214"
  val key = "robo-race"

  def get() = Action.async {
    load().map(values => Ok(values.asJson))
  }

  def post() = Action.async(circe.tolerantJson[GameScenario]) { request =>
    val sKey = UUID.randomUUID().toString
    val value = request.body
    for {
      resp <- add(sKey, value)
    } yield new Status(resp.status)(resp.body)
  }

  def postDefault() = Action.async {
    for {
      resp <- add("default", GameScenario.default)
    } yield new Status(resp.status)(resp.body)
  }

  private def add(key: String, value: GameScenario): Future[WSResponse] =
    for {
      state <- load()
      added = state + (key -> value)
      resp <- save(added)
    } yield resp

  private def save(scenarios: Map[String, GameScenario]): Future[WSResponse] =
    ws.url(s"https://api.keyvalue.xyz/$token/$key").post(scenarios.asJson.toString)

  private def load(): Future[Map[String, GameScenario]] =
    ws.url(s"https://api.keyvalue.xyz/$token/$key")
      .get()
      .map(resp => {
        Logger.info(resp.statusText)
        Logger.info(resp.body.asJson.as[Map[String, GameScenario]].toString)
        io.circe.parser.parse(resp.body).flatMap(_.as[Map[String, GameScenario]]).getOrElse(Map.empty)
      })
}
