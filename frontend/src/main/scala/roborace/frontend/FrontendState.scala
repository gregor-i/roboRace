package roborace.frontend

import roborace.frontend.editor.ClickAction
import gameEntities.{GameResponse, Instruction, Scenario, ScenarioResponse}

trait FrontendState

case class GameFrontendState(game: GameResponse, focusedSlot: Int = 0, slots: Map[Int, Instruction] = Map.empty) extends FrontendState

case class LobbyFrontendState(games: Seq[GameResponse], scenarios: Seq[ScenarioResponse]) extends FrontendState

case class PreviewFrontendState(scenario: ScenarioResponse) extends FrontendState

case class EditorState(scenario: Scenario, description: String = "", clickAction: Option[editor.ClickAction] = None) extends FrontendState
