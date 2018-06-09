package controller

import play.api.libs.circe.Circe
import play.api.mvc.InjectedController

class UiController() extends InjectedController {

  private def ui(mode: String, gameId: String = "", scenarioId: String = "") = Action(
    Ok(views.html.RoboRace(mode = mode, gameId = gameId, scenarioId = scenarioId))
  )

  def lobby() = ui(mode = "lobby")

  def game(id: String) = ui(mode = "game", gameId = id)

  def editor() = ui(mode = "editor")

  def editorWithId(id: String) = ui(mode = "editor", scenarioId = id)
}
