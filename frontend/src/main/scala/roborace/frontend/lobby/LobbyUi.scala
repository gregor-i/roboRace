//package roborace.frontend.lobby
//
//import com.raquo.snabbdom
//import com.raquo.snabbdom.Modifier
//import com.raquo.snabbdom.simple.attrs.id
//import com.raquo.snabbdom.simple.events.onClick
//import com.raquo.snabbdom.simple.implicits._
//import com.raquo.snabbdom.simple.props.{className, href, src}
//import com.raquo.snabbdom.simple.styles.cursor
//import com.raquo.snabbdom.simple.tags.{a, _}
//import com.raquo.snabbdom.simple.{VNode, VNodeData}
//import roborace.frontend.{LobbyState, Main}
//import roborace.frontend.components.BulmaComponents._
//import roborace.frontend.components.{Images, RobotImage}
//import roborace.frontend.util.Ui
//import gameEntities._
//
//object LobbyUi extends Ui {
//  def render(lobbyState: LobbyState): VNode =
//    div(id := "robo-race",
//      renderHeader(),
//      singleColumn(
//        seq(lobbyState.games.map(gameCard)),
//        seq(lobbyState.scenarios.map(scenarioCard))
//      )
//    )
//
//  def renderHeader(): VNode =
//    snabbdom.simple.tags.build("nav")(className := "navbar is-light",
//      div(className := "navbar-brand",
//        a(className := "navbar-item",
//          href := "/",
//          img(src := Images.logo)
//        ),
//        a(className := "navbar-item",
//          href := "#",
//          "Tutorial"
//        )
//      )
//    )
//
//  def gameCard(gameResponse: GameResponse): VNode = {
//    val youTag: Modifier[VNode, VNodeData] = gameResponse.you match {
//      case Some(you: QuittedPlayer)                                 => tag("Quitted", "is-danger")
//      case Some(you: FinishedPlayer)                                => tag(s"Finished as ${you.rank}", "is-primary")
//      case Some(you: RunningPlayer) if you.instructionSlots.isEmpty => tag("Awaits your instructions", "is-warning")
//      case Some(you: RunningPlayer)                                 => tag("Awaiting other players instructions")
//      case _                                                        => None
//    }
//
//    card(
//      mediaObject(Some(RobotImage(gameResponse.id.hashCode().abs % 6, filled = true)),
//        div(className := "tags are-large",
//          tag(s"Size: ${gameResponse.robots.size} / ${gameResponse.scenario.initialRobots.size} players", "is-info"),
//          youTag,
//          cond(gameResponse.robots.size < gameResponse.scenario.initialRobots.size && gameResponse.cycle == 0,
//            tag("Open for new player", "is-primary"))
//        ),
//        onClick := (_ => Main.gotoGame(gameResponse)),
//        cursor := "pointer"
//      ),
//      "Enter" -> Some(_ => Main.gotoGame(gameResponse)),
//      "Quit" -> None
//    )
//  }
//
//  def scenarioCard(scenarioResponse: ScenarioResponse): VNode =
//    card(
//      mediaObject(Some(RobotImage(scenarioResponse.id.hashCode().abs % 6, filled = true)),
//        div(className := "tags are-large",
//          tag(s"Description: ${scenarioResponse.description}", "is-info"),
//          tag(s"Size: ${scenarioResponse.scenario.initialRobots.size} players", "is-info"),
//          cond(scenarioResponse.scenario.traps.nonEmpty, tag(s"Contains traps", "is-warning")),
//          cond(scenarioResponse.ownedByYou, tag(s"Created by you", "is-info"))
//        ),
//        cursor := "pointer",
//        onClick := (_ => Main.gotoPreviewScenario(scenarioResponse))
//      ),
//      "Start Game" -> Some(_ => Main.gotoPreviewScenario(scenarioResponse)),
//      "Editor" -> Some(_ => Main.gotoEditor(scenarioResponse)),
//      "Delete" -> None
//    )
//
//}
//
