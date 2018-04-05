package controller

import gameLogic.GameScenario
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController

class UiController() extends InjectedController with Circe {

  def index() = Action(
    Ok(views.html.RoboRace())
  )

  def defaultScenario() = Action {
    Ok(GameScenario.default.asJson)
  }
}
