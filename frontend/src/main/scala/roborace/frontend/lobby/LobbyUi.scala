package roborace.frontend.lobby

import gameEntities._
import roborace.frontend.components.{BulmaComponents, Images, RobotImage}
import roborace.frontend.{LobbyFrontendState, Main}
import snabbdom.{Node, VNode}

object LobbyUi {
  def render(lobbyState: LobbyFrontendState): Node =
    Node("div.robo-race")
      .child(renderHeader())
      .child(
        BulmaComponents.singleColumn(
          lobbyState.games.map(gameCard) ++ lobbyState.scenarios.map(scenarioCard)
//        lobbyState.scenarios.map(scenarioCard)
        )
      )

  def renderHeader(): Node =
    Node("nav.navbar.is-light")
      .child(
        Node("div")
          .classes("navbar-brand")
          .child(
            Node("a")
              .classes("navbar-item")
              .attr("href", "/")
              .child(Node("img").attr("src", Images.logo))
          )
      )
      .child(
        Node("a.navbar-item")
          .attr("href", "#")
          .text("Tutorial")
      )

  def gameCard(gameResponse: GameResponse): Node = {
//    val youTag: Modifier[VNode, VNodeData] = gameResponse.you match {
//      case Some(you: QuittedPlayer)                                 => tag("Quitted", "is-danger")
//      case Some(you: FinishedPlayer)                                => tag(s"Finished as ${you.rank}", "is-primary")
//      case Some(you: RunningPlayer) if you.instructionSlots.isEmpty => tag("Awaits your instructions", "is-warning")
//      case Some(you: RunningPlayer)                                 => tag("Awaiting other players instructions")
//      case _                                                        => None
//    }

    BulmaComponents.card(
      BulmaComponents.mediaObject(
        Some(RobotImage(gameResponse.id.hashCode().abs % 6, filled = true)),
        Node("div.tags.are-large")
//            .child(          tag(s"Size: ${gameResponse.robots.size} / ${gameResponse.scenario.initialRobots.size} players", "is-info"))
//          .child(youTag)
//          .childOptional(
//            gameResponse.robots.size < gameResponse.scenario.initialRobots.size && gameResponse.cycle == 0,
//            tag("Open for new player", "is-primary")
//          )
      )
//        onClick := (_ => Main.gotoGame(gameResponse)),
//        cursor := "pointer"
//      "Enter" -> Some(_ => Main.gotoGame(gameResponse)),
//      "Quit"  -> None
    )
  }

  def scenarioCard(scenarioResponse: ScenarioResponse): Node =
    BulmaComponents.card(
      BulmaComponents
        .mediaObject(
          Some(RobotImage(scenarioResponse.id.hashCode().abs % 6, filled = true)),
          Node("div.tags.are-large")
//          .child(tag(s"Description: ${scenarioResponse.description}", "is-info"))
//          .child(tag(s"Size: ${scenarioResponse.scenario.initialRobots.size} players", "is-info"))
//          cond(scenarioResponse.scenario.traps.nonEmpty, tag(s"Contains traps", "is-warning")),
//          cond(scenarioResponse.ownedByYou, tag(s"Created by you", "is-info"))
        )
        .style("cursor", "pointer")
//        onClick := (_ => Main.gotoPreviewScenario(scenarioResponse))
//      "Start Game" -> Some(_ => Main.gotoPreviewScenario(scenarioResponse)),
//      "Editor" -> Some(_ => Main.gotoEditor(scenarioResponse)),
//      "Delete" -> None
    )

}
