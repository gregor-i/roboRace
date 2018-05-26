package controller

import play.api.libs.circe.Circe
import play.api.mvc.InjectedController

class UiController() extends InjectedController with Circe {

  def lobby() = Action(
    Ok(views.html.RoboRace(mode = "lobby"))
  )

  def game(id: String) = Action(
    Ok(views.html.RoboRace(mode = "game", gameId = id))
  )

  def editor() =  Action(
    Ok(views.html.RoboRace(mode = "editor"))
  )
}
