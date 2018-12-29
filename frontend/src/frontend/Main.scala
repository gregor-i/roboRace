package frontend

import frontend.editor.Editor
import frontend.game.Game
import frontend.lobby.Lobby
import frontend.preview.Preview
import gameEntities.{GameResponse, ScenarioResponse}
import org.scalajs.dom
import org.scalajs.dom.{Element, Event}

object Main {
  def container: Element = dom.document.getElementById("robo-race")

  def gotoLobby(): Unit = {
    new Lobby(container)
  }

  def gotoEditor(scenario: ScenarioResponse): Unit = {
    new Editor(container, scenario)
  }

  def gotoGame(game: GameResponse): Unit = {
    new Game(container, game)
  }

  def gotoPreviewScenario(scenario: ScenarioResponse): Unit = {
    new Preview(container, scenario)
  }

  def main(args: Array[String]): Unit = {
    dom.document.addEventListener[Event]("DOMContentLoaded", _ => gotoLobby())
  }
}
