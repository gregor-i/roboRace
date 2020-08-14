package roborace.frontend.pages.editor

import gameEntities._
import roborace.frontend.FrontendState
import roborace.frontend.pages.components.gameBoard.RenderScenario
import roborace.frontend.pages.components.{Body, Fab, Icons, Images}
import roborace.frontend.pages.lobby.LobbyPage
import roborace.frontend.service.{Actions, Service}
import roborace.frontend.util.Untyped
import snabbdom.{Node, Snabbdom}

object EditorUi {
  def apply(implicit state: EditorState, update: FrontendState => Unit): Node = {
    Body
      .game()
      .child(Fab(Icons.close).classes("fab-right-1").event("click", Snabbdom.event(_ => update(LobbyPage.load()))))
      .child(RenderScenario(state.scenario, clickListener(state, update)))
      .child(actionbar(state, update))
  }

  private def clickListener(state: EditorState, update: FrontendState => Unit): Option[(Position, Direction) => Unit] =
    state.clickAction.map { action => (position: Position, direction: Direction) =>
      update(EditorState.scenario.modify(action.apply(position, direction))(state))
    }

  private def actionbar(state: EditorState, update: EditorState => Unit): Node = {
    def icon(url: String) = Node("img").style("height", "100%").attr("src", url)

    val scenario = state.scenario

    def textButton(text: String, action: => EditorState): Node =
      Node("button.button.is-light").text(text).event("click", Snabbdom.event(_ => update(action)))

    def iconButton(icon: Node, action: => EditorState): Node =
      Node("button.button.is-light").child(icon).event("click", Snabbdom.event(_ => update(action)))

    Node("div.footer-group")
      .child(
        Node("div.text-panel")
          .children(
            textButton("W--", state.copy(scenario = scenario.copy(width = scenario.width - 1))),
            textButton("W++", state.copy(scenario = scenario.copy(width = scenario.width + 1))),
            textButton("H--", state.copy(scenario = scenario.copy(height = scenario.height - 1))),
            textButton("H++", state.copy(scenario = scenario.copy(height = scenario.height + 1))),
            textButton("Wall", state.copy(clickAction = Some(ToggleWall))),
            textButton("Pit", state.copy(clickAction = Some(TogglePit))),
            iconButton(icon(Images.trapTurnLeft), state.copy(clickAction = Some(ToggleTurnLeftTrap))),
            iconButton(icon(Images.trapTurnRight), state.copy(clickAction = Some(ToggleTurnRightTrap))),
            iconButton(icon(Images.trapStun), state.copy(clickAction = Some(ToggleStunTrap))),
            iconButton(icon(Images.target), state.copy(clickAction = Some(SetTarget))),
            iconButton(icon(Images.playerStart(0)), state.copy(clickAction = Some(ToggleInitialRobot))),
            iconButton(icon(Images.action(TurnRight)), state.copy(clickAction = Some(RotateRobot)))
          )
      )
      .child(
        Node("div.text-panel")
          .child(
            Node("div.field.has-addons")
              .child(
                Node("div.control is-expanded").child(
                  Node("input.input")
                    .attr("type", "text")
                    .attr("placeholder", "description")
                    .attr("value", state.description)
                    .event("change", Snabbdom.event(e => update(state.copy(description = Untyped(e).target.value.asInstanceOf[String]))))
                )
              )
              .child(
                Node("div.control")
                  .child(
                    Node("button.button.is-primary")
                      .event("click", Snabbdom.event(_ => Actions.saveScenario(ScenarioPost(state.description, state.scenario))))
                      .text("Save Scenario")
                  )
              )
          )
      )
  }
}
