//package roborace.frontend.editor
//
//import com.raquo.snabbdom.simple.VNode
//import com.raquo.snabbdom.simple.events.{onChange, onClick}
//import com.raquo.snabbdom.simple.implicits._
//import com.raquo.snabbdom.simple.props.{className, src}
//import com.raquo.snabbdom.simple.attrs.{`type`, id, placeholder, value}
//import com.raquo.snabbdom.simple.styles.height
//import com.raquo.snabbdom.simple.tags.{button, div, img, input}
//import roborace.frontend.{EditorState, Main, Service}
//import roborace.frontend.components.{Fab, Images}
//import roborace.frontend.gameBoard.RenderScenario
//import roborace.frontend.util.Dynamic
//import gameEntities._
//
//
//object EditorUi {
//  def apply(state: EditorState, rerender: EditorState => Unit): VNode = {
//    div(id := "robo-race",
//      className := "game",
//      Fab("fab-right-1", Images.iconClose, onClick := (() => Main.gotoLobby())),
//      RenderScenario(state.scenario, clickListener(state, rerender)),
//      renderEditorActionbar(state, rerender)
//    )
//  }
//
//  def clickListener(state: EditorState, rerender: EditorState => Unit) =
//    state.clickAction.map { action =>
//      (position: Position, direction: Direction) =>
//        val scenario = state.scenario
//        rerender(action match {
//          case ToggleWall          =>
//            val wall = direction match {
//              case Up                              => Wall(Position(position.x, position.y - 1), Down)
//              case UpLeft if position.x % 2 == 0   => Wall(Position(position.x - 1, position.y - 1), DownRight)
//              case UpLeft                          => Wall(Position(position.x - 1, position.y), DownRight)
//              case DownLeft if position.x % 2 == 0 => Wall(Position(position.x - 1, position.y), UpRight)
//              case DownLeft                        => Wall(Position(position.x - 1, position.y + 1), UpRight)
//              case w: WallDirection                => Wall(position, w)
//            }
//            state.copy(scenario = scenario.copy(walls =
//              if (scenario.walls.contains(wall))
//                scenario.walls.filter(_ != wall)
//              else
//                scenario.walls :+ wall
//            ))
//          case TogglePit           =>
//            state.copy(scenario = scenario.copy(pits =
//              if (scenario.pits.contains(position))
//                scenario.pits.filter(_ != position)
//              else
//                scenario.pits :+ position
//            ))
//          case ToggleTurnLeftTrap  =>
//            state.copy(scenario = scenario.copy(traps =
//              if (scenario.traps.exists(_.position == position))
//                scenario.traps.filter(_.position != position)
//              else
//                scenario.traps :+ TurnLeftTrap(position)
//            ))
//          case ToggleTurnRightTrap =>
//            state.copy(scenario = scenario.copy(traps =
//              if (scenario.traps.exists(_.position == position))
//                scenario.traps.filter(_.position != position)
//              else
//                scenario.traps :+ TurnRightTrap(position)
//            ))
//          case ToggleStunTrap      =>
//            state.copy(scenario = scenario.copy(traps =
//              if (scenario.traps.exists(_.position == position))
//                scenario.traps.filter(_.position != position)
//              else
//                scenario.traps :+ StunTrap(position)
//            ))
//          case SetTarget           =>
//            state.copy(scenario = scenario.copy(targets =
//              if (scenario.targets.contains(position))
//                scenario.targets.filter(_ != position)
//              else
//                scenario.targets :+ position
//            ))
//          case ToggleInitialRobot  =>
//            state.copy(scenario = scenario.copy(initialRobots =
//              (if (scenario.initialRobots.exists(_.position == position))
//                scenario.initialRobots.filter(_.position != position)
//              else
//                scenario.initialRobots :+ Robot(0, position, Up))
//                .zipWithIndex.map { case (robot, index) => robot.copy(index = index) }
//            ))
//          case RotateRobot         =>
//            state.copy(scenario = scenario.copy(initialRobots =
//              scenario.initialRobots.map(r =>
//                if (r.position == position) r.copy(direction = turnRight(r.direction))
//                else r
//              )))
//        })
//    }
//
//  def renderEditorActionbar(state: EditorState, rerender: EditorState => Unit) = {
//    def icon(url: String) = img(height := "100%", src := url)
//
//    val scenario = state.scenario
//    val buttonClass = className := "button is-light"
//
//    div(className := "footer-group",
//      div(className := "text-panel",
//        button(buttonClass, "W--", onClick := (_ => rerender(state.copy(scenario = scenario.copy(width = scenario.width - 1))))),
//        button(buttonClass, "W++", onClick := (_ => rerender(state.copy(scenario = scenario.copy(width = scenario.width + 1))))),
//        button(buttonClass, "H--", onClick := (_ => rerender(state.copy(scenario = scenario.copy(height = scenario.height - 1))))),
//        button(buttonClass, "H++", onClick := (_ => rerender(state.copy(scenario = scenario.copy(height = scenario.height + 1))))),
//        button(buttonClass, "Wall", onClick := (_ => rerender(state.copy(clickAction = Some(ToggleWall))))),
//        button(buttonClass, "Pit", onClick := (_ => rerender(state.copy(clickAction = Some(TogglePit))))),
//        button(buttonClass, icon(Images.trapTurnLeft), onClick := (_ => rerender(state.copy(clickAction = Some(ToggleTurnLeftTrap))))),
//        button(buttonClass, icon(Images.trapTurnRight), onClick := (_ => rerender(state.copy(clickAction = Some(ToggleTurnRightTrap))))),
//        button(buttonClass, icon(Images.trapStun), onClick := (_ => rerender(state.copy(clickAction = Some(ToggleStunTrap))))),
//        button(buttonClass, icon(Images.target), onClick := (_ => rerender(state.copy(clickAction = Some(SetTarget))))),
//        button(buttonClass, icon(Images.playerStart(0)), onClick := (_ => rerender(state.copy(clickAction = Some(ToggleInitialRobot))))),
//        button(buttonClass, icon(Images.action(TurnRight)), onClick := (_ => rerender(state.copy(clickAction = Some(RotateRobot)))))
//      ),
//      div(className := "text-panel",
//        div(className := "field has-addons",
//          div(className := "control is-expanded",
//            input(className := "input",
//              `type` := "text",
//              placeholder := "description",
//              value := state.description,
//              onChange := (e => rerender(state.copy(description = Dynamic(e).target.value.asInstanceOf[String])))
//            )
//          ),
//          div(className := "control",
//            button(className := "button is-primary",
//              onClick := (_ => Service.saveScenario(ScenarioPost(state.description, state.scenario))),
//              "Save Scenario")
//          )
//        )
//      )
//    )
//  }
//
//
//  def turnRight(dir: Direction): Direction = dir match {
//    case Up        => UpRight
//    case UpRight   => DownRight
//    case DownRight => Down
//    case Down      => DownLeft
//    case DownLeft  => UpLeft
//    case UpLeft    => Up
//  }
//}
