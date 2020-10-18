package roborace.frontend.pages
package editor

import api.Entity
import entities._
import logic.Direction
import roborace.frontend.pages.components.gameBoard.RenderScenario
import roborace.frontend.pages.components.{Body, Fab, Icons, Images}
import roborace.frontend.pages.editor.EditorPage.Context
import roborace.frontend.pages.multiplayer.lobby.LobbyPage
import roborace.frontend.service.Actions
import roborace.frontend.util.{SnabbdomEventListener, Untyped}
import snabbdom.{Node, Snabbdom}

object EditorUi {
  def apply(implicit context: Context): Node = {
    Body
      .game()
      .child(Fab(Icons.close).classes("fab-right-1").event("click", SnabbdomEventListener.set(LobbyPage.load())))
      .child(RenderScenario(context.local.scenario, clickListener()))
      .child(actionbar())
      .child(descriptionAndSave)
  }

  private def clickListener()(implicit context: Context): Option[(Position, Direction) => Unit] =
    context.local.clickAction.map { action => (position: Position, direction: Direction) =>
      context.update(EditorState.scenario.modify(action.apply(position, direction))(context.local))
    }

  private def actionbar()(implicit context: Context): Node = {
    def icon(url: String) = Node("img").style("height", "100%").attr("src", url)

    val scenario = context.local.scenario

    def textButton(text: String, action: => EditorState): Node =
      Node("button.button.is-light").text(text).event("click", SnabbdomEventListener.set(action))

    def iconButton(icon: Node, action: => EditorState): Node =
      Node("button.button.is-light").child(icon).event("click", SnabbdomEventListener.set(action))

    Node("div.nowrap-panel")
      .style("margin", "8px")
      .children(
        textButton("W--", context.local.copy(scenario = removeOutsideObjects(scenario.copy(width = (scenario.width - 1).max(1))))),
        textButton("W++", context.local.copy(scenario = scenario.copy(width = scenario.width + 1))),
        textButton("H--", context.local.copy(scenario = removeOutsideObjects(scenario.copy(height = (scenario.height - 1).max(1))))),
        textButton("H++", context.local.copy(scenario = scenario.copy(height = scenario.height + 1))),
        textButton("Wall", context.local.copy(clickAction = Some(ToggleWall))),
        textButton("Pit", context.local.copy(clickAction = Some(TogglePit))),
        iconButton(icon(Images.trapTurnLeft), context.local.copy(clickAction = Some(ToggleTurnLeftTrap))),
        iconButton(icon(Images.trapTurnRight), context.local.copy(clickAction = Some(ToggleTurnRightTrap))),
        iconButton(icon(Images.trapStun), context.local.copy(clickAction = Some(ToggleStunTrap))),
        iconButton(icon(Images.trapPushUp), context.local.copy(clickAction = Some(TogglePushTrap))),
        iconButton(icon(Images.target), context.local.copy(clickAction = Some(SetTarget))),
        iconButton(icon(Images.playerStart(0)), context.local.copy(clickAction = Some(ToggleInitialRobot))),
        iconButton(icon(Images.instructionIcon(TurnRight)), context.local.copy(clickAction = Some(RotateRobot)))
      )
  }

  private def descriptionAndSave(implicit context: Context) =
    Node("div.nowrap-panel")
      .style("margin", "8px")
      .child(
        Node("div.field.has-addons")
          .child(
            Node("div.control is-expanded").child(
              Node("input.input")
                .attr("type", "text")
                .attr("placeholder", "description")
                .attr("value", context.local.description)
                .event("change", Snabbdom.event(e => context.update(context.local.copy(description = Untyped(e).target.value.asInstanceOf[String]))))
            )
          )
          .child(
            Node("div.control")
              .child(
                Node("button.button.is-primary")
                  .event(
                    "click",
                    SnabbdomEventListener
                      .sideeffect(() => Actions.saveScenario(Entity(description = context.local.description, value = context.local.scenario)))
                  )
                  .text("Save Scenario")
              )
          )
      )

  private def removeOutsideObjects(scenario: Scenario): Scenario = {
    def inside(position: Position): Boolean =
      position.x < scenario.width && position.y < scenario.height

    scenario.copy(
      traps = scenario.traps.filter(t => inside(t.position)),
      pits = scenario.pits.filter(inside),
      initialRobots = scenario.initialRobots.filter(r => inside(r.position)).zipWithIndex.map { case (robot, index) => robot.copy(index = index) },
      targets = scenario.targets.filter(inside),
      walls = scenario.walls.filter { w =>
        inside(w.position) || inside(Direction.move(w.direction, w.position))
      }
    )
  }
}
