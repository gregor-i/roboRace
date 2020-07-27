package roborace.frontend

import roborace.frontend.editor.ClickAction
import gameEntities.{GameResponse, Instruction, Scenario, ScenarioResponse}

trait FrontendState

case class EditorFrontendState(scenario: Scenario, clickAction: Option[ClickAction], description: String) extends FrontendState

case class GameFrontendState(game: GameResponse, focusedSlot: Int, slots: Map[Int, Instruction]) extends FrontendState

case class LobbyFrontendState(games: Seq[GameResponse], scenarios: Seq[ScenarioResponse]) extends FrontendState

case class PreviewFrontendState(scenario: ScenarioResponse) extends FrontendState
