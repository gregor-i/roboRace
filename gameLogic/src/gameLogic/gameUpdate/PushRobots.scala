package gameLogic.gameUpdate

import gameLogic.{Direction, Logged, Position, Robot, RobotPositionTransition}

object PushRobots {

  def apply(position: Position, direction: Direction, robots: Map[String, Robot]): Logged[Map[String, Robot]] =
    robots.find(_._2.position == position) match {
      case Some((player, robot)) =>
        val nextPos  = robot.position.move(direction)
        for {
          pushedRobots <- PushRobots(position.move(direction), direction, robots)
          nextRobotState <- robot.copy(position = nextPos).log(RobotPositionTransition(player, robot.position, nextPos))
        } yield pushedRobots + (player -> nextRobotState)
      case None => Logged.pure(robots)
    }
}
