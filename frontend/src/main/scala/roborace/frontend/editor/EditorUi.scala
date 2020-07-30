package roborace.frontend.editor

import gameEntities._
import roborace.frontend.FrontendState
import roborace.frontend.components.{Fab, Images}
import roborace.frontend.components.gameBoard.RenderScenario
import roborace.frontend.lobby.LobbyPage
import roborace.frontend.service.Service
import roborace.frontend.util.Untyped
import snabbdom.{Node, Snabbdom}

object EditorUi {
  def apply(state: EditorState, rerender: FrontendState => Unit): Node = {
    Node("div.game")
      .prop("id", "robo-race")
      .child(Fab(Images.iconClose).classes("fab-right-1").event("click", Snabbdom.event(_ => rerender(LobbyPage.load()))))
      .child(RenderScenario(state.scenario, clickListener(state, rerender)))
      .child(renderEditorActionbar(state, rerender))
  }

  def clickListener(state: EditorState, update: FrontendState => Unit): Option[(Position, Direction) => Unit] =
    state.clickAction.map { action => (position: Position, direction: Direction) =>
      val scenario = state.scenario
      update(action match {
        case ToggleWall =>
          val wall = direction match {
            case Up => Wall(Position(position.x, position.y - 1), Down)
            case UpLeft if position.x % 2 == 0 => Wall(Position(position.x - 1, position.y - 1), DownRight)
            case UpLeft => Wall(Position(position.x - 1, position.y), DownRight)
            case DownLeft if position.x % 2 == 0 => Wall(Position(position.x - 1, position.y), UpRight)
            case DownLeft         => Wall(Position(position.x - 1, position.y + 1), UpRight)
            case w: WallDirection => Wall(position, w)
          }
          state.copy(
            scenario = scenario.copy(
              walls =
                if (scenario.walls.contains(wall))
                  scenario.walls.filter(_ != wall)
                else
                  scenario.walls :+ wall
            )
          )
        case TogglePit =>
          state.copy(
            scenario = scenario.copy(
              pits =
                if (scenario.pits.contains(position))
                  scenario.pits.filter(_ != position)
                else
                  scenario.pits :+ position
            )
          )
        case ToggleTurnLeftTrap =>
          state.copy(
            scenario = scenario.copy(
              traps =
                if (scenario.traps.exists(_.position == position))
                  scenario.traps.filter(_.position != position)
                else
                  scenario.traps :+ TurnLeftTrap(position)
            )
          )
        case ToggleTurnRightTrap =>
          state.copy(
            scenario = scenario.copy(
              traps =
                if (scenario.traps.exists(_.position == position))
                  scenario.traps.filter(_.position != position)
                else
                  scenario.traps :+ TurnRightTrap(position)
            )
          )
        case ToggleStunTrap =>
          state.copy(
            scenario = scenario.copy(
              traps =
                if (scenario.traps.exists(_.position == position))
                  scenario.traps.filter(_.position != position)
                else
                  scenario.traps :+ StunTrap(position)
            )
          )
        case SetTarget =>
          state.copy(
            scenario = scenario.copy(
              targets =
                if (scenario.targets.contains(position))
                  scenario.targets.filter(_ != position)
                else
                  scenario.targets :+ position
            )
          )
        case ToggleInitialRobot =>
          state.copy(
            scenario = scenario.copy(
              initialRobots = (if (scenario.initialRobots.exists(_.position == position))
                                 scenario.initialRobots.filter(_.position != position)
                               else
                                 scenario.initialRobots :+ Robot(0, position, Up)).zipWithIndex.map {
                case (robot, index) => robot.copy(index = index)
              }
            )
          )
        case RotateRobot =>
          state.copy(
            scenario = scenario.copy(
              initialRobots = scenario.initialRobots.map(
                r =>
                  if (r.position == position) r.copy(direction = turnRight(r.direction))
                  else r
              )
            )
          )
      })
    }

  def renderEditorActionbar(state: EditorState, update: EditorState => Unit): Node = {
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
                      .event("click", Snabbdom.event(_ => Service.saveScenario(ScenarioPost(state.description, state.scenario))))
                      .text("Save Scenario")
                  )
              )
          )
      )
  }

  def turnRight(dir: Direction): Direction = dir match {
    case Up        => UpRight
    case UpRight   => DownRight
    case DownRight => Down
    case Down      => DownLeft
    case DownLeft  => UpLeft
    case UpLeft    => Up
  }
}
