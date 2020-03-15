package roborace.frontend

import roborace.frontend.editor.ClickAction
import gameEntities.{GameResponse, Instruction, Scenario, ScenarioResponse}


sealed trait State

case class EditorState(scenario: Scenario,
                       clickAction: Option[ClickAction],
                       description: String) extends State

case class GameState(game: GameResponse,
                     focusedSlot: Int,
                     slots: Map[Int, Instruction]) extends State

case class LobbyState(games: Seq[GameResponse],
                      scenarios: Seq[ScenarioResponse]) extends State

case class PreviewState(scenario: ScenarioResponse) extends State
