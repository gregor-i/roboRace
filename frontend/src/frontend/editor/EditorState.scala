package frontend.editor

import gameEntities.Scenario

case class EditorState(scenario: Scenario,
                       clickAction: Option[ClickAction],
                       description: String)

sealed trait ClickAction
case object ToggleWall extends ClickAction
case object TogglePit extends ClickAction
case object ToggleTurnLeftTrap extends ClickAction
case object ToggleTurnRightTrap extends ClickAction
case object ToggleStunTrap extends ClickAction
case object SetTarget extends ClickAction
case object ToggleInitialRobot extends ClickAction
case object RotateRobot  extends ClickAction
