package frontend.lobby

import com.raquo.snabbdom.simple.VNode
import com.raquo.snabbdom.simple.events.onClick
import com.raquo.snabbdom.simple.implicits._
import com.raquo.snabbdom.simple.props.{className, disabled, href}
import com.raquo.snabbdom.simple.attrs.id
import com.raquo.snabbdom.simple.tags._
import frontend.Main
import frontend.common.{BulmaComponents, RenderRobotImages}
import frontend.util.Ui
import gameEntities.{GameResponse, ScenarioResponse}

object LobbyUi extends Ui {
  def render(lobbyState: LobbyState): VNode =
    div(id := "robo-race",
      renderHeader(),
      renderGameList(lobbyState),
      renderScenarioList(lobbyState)
    )

  def renderHeader() =
    div(className := "hero is-primary",
      div(className := "hero-head",
        div(className := "navbar",
          div(className := "navbar-end",
            a(className := "navbar-item",
              href := "https://github.com/gregor-i/roboRace",
              "Sources @ Github"
            )
          )
        )
      ),
      div(className := "hero-body",
        div(className := "container",
          h1(className := "title is-2", "Robo Race"),
          h2(className := "subtitle is-3", "Game Lobby")
        )
      )
    )

  def renderGameList(lobbyState: LobbyState) =
    div(className := "section",
      h4(className := "title", "Game List: "),
      seq(lobbyState.games.map(gameCard))
    )

  def gameState(gameResponse: GameResponse): String = {
    if (gameResponse.cycle == 0)
      "Game waiting for players"
    else
      "Game running"
  }


  def renderPlayerSlots(gameResponse: GameResponse) =
    seq(gameResponse.scenario.initialRobots.map(robot =>
      RenderRobotImages(
        robot.index,
        gameResponse.robots.exists(r => r.index == robot.index),
        gameResponse.you.exists(_.index == robot.index),
        None
      )
    ))

  def gameCard(gameResponse: GameResponse): VNode =
    BulmaComponents.card(BulmaComponents.mediaObject(None,
      div(
        div(strong("game state: "), gameState(gameResponse)),
        div(renderPlayerSlots(gameResponse)),
        button(className := "button is-light is-primary", "Enter", onClick := (_ => Main.gotoGame(gameResponse)))
      )))

  def scenarioCard(scenarioResponse: ScenarioResponse): VNode =
    BulmaComponents.card(BulmaComponents.mediaObject(None,
      div(
        div(strong("Scenario description: "), scenarioResponse.description),
        div(strong("Scenario size: "), s"for ${scenarioResponse.scenario.initialRobots.size} players"),
        div(
          button(className := "button is-primary", "Preview", onClick := (_ => Main.gotoPreviewScenario(scenarioResponse))),
          button(className := "button", "Edit", onClick := (_ => Main.gotoEditor(scenarioResponse))),
          button(className := "button", disabled := true, "Delete"),
        )
      )
    ))

  def renderScenarioList(state: LobbyState) =
    div(className := "section",
      h4(className := "title", "Scenario List: "),
      seq(state.scenarios.map(scenarioCard))
    )


}

