package roborace.frontend.pages.editor

import entities._
import logic.Direction
import roborace.frontend.pages.editor.Helper.toggle

sealed trait ClickAction {
  def apply(position: Position, direction: Direction): Scenario => Scenario
}
case object ToggleWall extends ClickAction {
  def apply(position: Position, direction: Direction): Scenario => Scenario =
    Scenario.walls.modify(toggle(Wall(position, direction)))
}
case object TogglePit extends ClickAction {
  def apply(position: Position, direction: Direction): Scenario => Scenario =
    Scenario.pits.modify(toggle(position))
}
case object ToggleTurnLeftTrap extends ClickAction {
  def apply(position: Position, direction: Direction): Scenario => Scenario =
    Scenario.traps.modify { traps =>
      if (traps.exists(_.position == position))
        traps.filter(_.position != position)
      else
        traps :+ TurnLeftTrap(position)
    }
}
case object ToggleTurnRightTrap extends ClickAction {
  def apply(position: Position, direction: Direction): Scenario => Scenario =
    Scenario.traps.modify { traps =>
      if (traps.exists(_.position == position))
        traps.filter(_.position != position)
      else
        traps :+ TurnRightTrap(position)
    }
}
case object ToggleStunTrap extends ClickAction {
  def apply(position: Position, direction: Direction): Scenario => Scenario =
    Scenario.traps.modify { traps =>
      if (traps.exists(_.position == position))
        traps.filter(_.position != position)
      else
        traps :+ StunTrap(position)
    }
}
case object SetTarget extends ClickAction {
  def apply(position: Position, direction: Direction): Scenario => Scenario =
    Scenario.targets.modify(toggle(position))
}
case object ToggleInitialRobot extends ClickAction {
  def apply(position: Position, direction: Direction): Scenario => Scenario =
    Scenario.initialRobots.modify { initialRobots =>
      if (initialRobots.exists(_.position == position))
        initialRobots
          .filter(_.position != position)
          .zipWithIndex
          .map {
            case (robot, index) => robot.copy(index = index)
          }
      else
        initialRobots :+ Robot(initialRobots.size, position, Up)
    }
}
case object RotateRobot extends ClickAction {
  def apply(position: Position, direction: Direction): Scenario => Scenario =
    Scenario.initialRobots.modify {
      _.map { robot =>
        if (robot.position == position) robot.copy(direction = Direction.turnRight(robot.direction))
        else robot
      }
    }
}

private object Helper {
  def toggle[A](value: A)(seq: Seq[A]): Seq[A] =
    if (seq.contains(value))
      seq.filter(_ != value)
    else
      seq :+ value
}
