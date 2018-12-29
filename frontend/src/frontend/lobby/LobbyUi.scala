package frontend.lobby

import com.raquo.snabbdom.Modifier
import com.raquo.snabbdom.simple.{VNode, VNodeData}
import com.raquo.snabbdom.simple.events.onClick
import com.raquo.snabbdom.simple.implicits._
import com.raquo.snabbdom.simple.props.{className, disabled, href, src}
import com.raquo.snabbdom.simple.attrs.id
import com.raquo.snabbdom.simple.tags._
import frontend.Main
import frontend.common.{BulmaComponents, Images, RenderRobotImages}
import frontend.util.Ui
import gameEntities.{GameResponse, ScenarioResponse}

object LobbyUi extends Ui {
  def render(lobbyState: LobbyState): VNode =
    div(id := "robo-race",
      renderHeader(),
      seq(lobbyState.games
        .filter(_.robots.nonEmpty)
        .map(gameCard)),
      seq(lobbyState.scenarios.map(scenarioCard))
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

  def gameCard(gameResponse: GameResponse): VNode = {
   val youTag: Modifier[VNode, VNodeData] = gameResponse.you match {
     case Some(you) if you.finished.exists(_.rageQuitted) => BulmaComponents.tag("Quitted", "is-danger")
     case Some(you) if you.finished.exists(!_.rageQuitted) => BulmaComponents.tag(s"Finished as ${you.finished.get.rank}", "is-primary")
     case Some(you) if you.instructionSlots.isEmpty => BulmaComponents.tag("Awaits your instructions", "is-warning")
     case _ => None
   }

    BulmaComponents.card(BulmaComponents.mediaObject(Some(RenderRobotImages(gameResponse.id.hashCode().abs % 6, filled = true)),
      div(className := "tags are-large",
        BulmaComponents.tag(s"Size: ${gameResponse.robots.size} / ${gameResponse.scenario.initialRobots.size} players", "is-info"),
        youTag,
        cond(gameResponse.robots.size < gameResponse.scenario.initialRobots.size && gameResponse.cycle == 0,
          BulmaComponents.tag("Open for new player", "is-primary"))
      )),
      Seq(
        a("Enter", onClick := (_ => Main.gotoGame(gameResponse))),
        a("Quit", onClick := (_ => println("todo")))
      )
    )
  }

  def scenarioCard(scenarioResponse: ScenarioResponse): VNode =
    BulmaComponents.card(BulmaComponents.mediaObject(Some(RenderRobotImages(scenarioResponse.id.hashCode().abs % 6, filled = true)),
      div(className := "tags are-large",
        BulmaComponents.tag(s"Description: ${scenarioResponse.description}", "is-info"),
        BulmaComponents.tag(s"Size: ${scenarioResponse.scenario.initialRobots.size} players", "is-info"),
        cond(scenarioResponse.scenario.traps.nonEmpty, BulmaComponents.tag(s"Contains traps", "is-warning")),
        cond(scenarioResponse.ownedByYou, BulmaComponents.tag(s"Created by you", "is-info"))
      )
    ), Seq(
      a("Start Game", onClick := (_ => Main.gotoPreviewScenario(scenarioResponse))),
      a("Editor", onClick := (_ => Main.gotoEditor(scenarioResponse))),
      a("Delete", onClick := (_ => println("todo"))),
    ))

}

