package gameLogic
package gameUpdate

import gameLogic.action._

object Cycle{
  def apply(gameState: GameState): Logged[GameState] = gameState match {
    case g: GameRunning if g.robotActions.keySet == g.players.toSet =>
      for {
        game <- g.log(AllPlayerDefinedActions)
        playerActions = g.players.map { player =>
          val robotAction = g.robotActions(player)
          applyAction(player, robotAction)(_)
        }
        afterPlayerActions <- Logged.flatMapFold(Logged.pure(game))(playerActions)
        nextCycle <- afterPlayerActions.copy(cycle = g.cycle + 1, robotActions = Map.empty).log(PlayerActionsExecuted(game.cycle + 1))
      } yield nextCycle

    case _ => Logged.pure(gameState)
  }

  private def applyAction(player: String, action: Action)(game: GameRunning): Logged[GameRunning] =
    for {
      robot <- game.robots(player).log(RobotAction(player, action))
      afterAction <- action match {
        case turn: TurnAction => turnAction(player, robot, turn, game)
        case move: MoveAction => moveAction(player, robot, move, game)
      }
      afterEffects <- environmentEffects(afterAction)
    } yield afterEffects

  private def turnAction(player: String, robot: Robot, action: TurnAction, game: GameRunning): Logged[GameRunning] = {
    val nextDirection = action match {
      case TurnLeft => robot.direction.left
      case TurnRight => robot.direction.right
    }
    for {
      nextRobotState <- robot.copy(direction = nextDirection).log(RobotDirectionTransition(player, robot.direction, nextDirection))
    } yield game.copy(robots = game.robots.updated(player, nextRobotState))
  }

  private def moveAction(player: String, robot: Robot, action: MoveAction, game: GameRunning): Logged[GameRunning] = {
    val direction = action match {
      case MoveForward => robot.direction
      case MoveBackward => robot.direction.back
    }
    for {
      updatedRobots <- if (movementIsAllowed(game, robot.position, direction))
        pushRobots(robot.position, direction, game.robots)
      else
        game.robots.log(RobotMovementBlocked(player, robot.position, direction))
    } yield game.copy(robots = updatedRobots)
  }

  private def pushRobots(position: Position, direction: Direction, robots: Map[String, Robot]): Logged[Map[String, Robot]] =
    robots.find(_._2.position == position) match {
      case Some((player, robot)) =>
        val nextPos  = robot.position.move(direction)
        for {
          pushedRobots <- pushRobots(position.move(direction), direction, robots)
          nextRobotState <- robot.copy(position = nextPos).log(RobotPositionTransition(player, robot.position, nextPos))
      } yield pushedRobots + (player -> nextRobotState)
      case None => Logged.pure(robots)
    }

  private def movementIsAllowed(game: GameRunning, position: Position, direction: Direction): Boolean = {
    val downWalls = game.scenario.walls.filter(_.direction == Down).map(_.position)
    val rightWalls = game.scenario.walls.filter(_.direction == Right).map(_.position)
    if (game.scenario.beaconPosition == position.move(direction))
      false
    else if (direction == Down && downWalls.contains(position))
      false
    else if (direction == Right && rightWalls.contains(position))
      false
    else if (direction == Up && downWalls.contains(position.move(Up)))
      false
    else if (direction == Left && downWalls.contains(position.move(Left)))
      false
    else if (game.robots.exists(_._2.position == position.move(direction)))
      movementIsAllowed(game, position.move(direction), direction)
    else
      true
  }

  private def environmentEffects(game: GameRunning): Logged[GameRunning] = {
    def resetRobot(player: String, robot: Robot)(robots: Robots): Logged[Robots] = {
      val index = game.players.zipWithIndex.find(_._1 == player).get._2
      val initial = game.scenario.initialRobots(index)
      for {
        clearedInitial <- pushRobots(initial.position, initial.direction, robots)
        resettedFallen <- (clearedInitial + (player -> initial)).log(RobotReset(player, robot, initial))
      } yield resettedFallen
    }

    val actions = game.robots.filter {
      case (_, robot) => robot.position.x >= game.scenario.width ||
        robot.position.x < 0 ||
        robot.position.y >= game.scenario.height ||
        robot.position.y < 0
    }.map { case (player, robot) =>
      resetRobot(player, robot)(_)
    }

    for {
      updatedRobots <- Logged.flatMapFold[Robots, EventLog](Logged.pure(game.robots))(actions)
    } yield game.copy(robots = updatedRobots)
  }
}
