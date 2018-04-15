package gameLogic
package gameUpdate

import gameLogic.action._

object Cycle{
  def apply(gameState: GameState): Logged[GameState] = gameState match {
    case g: GameRunning if g.players.forall(player => g.robotActions.isDefinedAt(player) && g.robotActions(player).length == 5) =>
      for {
        _ <- ().log(AllPlayerDefinedActions)
        afterPlayerActions <- execAllActions(g)
        afterEffects <- ScenarioEffects.afterCycle(afterPlayerActions)
        nextState <- afterEffects match {
          case running if running.players.isEmpty => GameFinished(running.finishedPlayers).log(AllPlayersFinished)
          case running => afterEffects.asInstanceOf[GameRunning].copy(cycle = g.cycle + 1).log(PlayerActionsExecuted(g.cycle + 1))
        }
      } yield nextState

    case _ => Logged.pure(gameState)
  }

  private def execAllActions(gameRunning: GameRunning): Logged[GameRunning] = {
    for {
      maybeNextPlayer <- calcNextPlayer(gameRunning)
      nextState <- maybeNextPlayer match {
        case Some(nextPlayer) => for {
          afterAction <- applyAction(gameRunning, nextPlayer)
          afterRecursion <- execAllActions(afterAction)
        } yield afterRecursion
        case None => Logged.pure(gameRunning)
      }
    } yield nextState
  }

  private def calcNextPlayer(gameState: GameRunning): Logged[Option[String]] = {
    val beacon = gameState.scenario.beaconPosition

    def distance(position: Position): Double = {
      val dx = position.x - beacon.x
      val dy = position.y - beacon.y
      Math.sqrt(dx * dx + dy * dy)
    }

    def angle(position: Position): Double = {
      val dx = position.x - beacon.x
      val dy = position.y - beacon.y
      Math.atan2(dx, dy)
    }

    def emptiedSlots(actions: Seq[Action]): Int = ActionSlots.actionsPerCycle - actions.size

    def nextPlayerWeight(player: String): (Int, Double, Double) = {
      val position = gameState.robots(player).position
      (emptiedSlots(gameState.robotActions(player)), distance(position), angle(position))
    }

    if (gameState.robotActions.forall(_._2.isEmpty)) {
       None.log()
    } else {
      val p = gameState.players.minBy(nextPlayerWeight)
      Some(p).log(NextRobotForActionDefined(p, gameState.players.map(player => player -> nextPlayerWeight(player)).toMap))
    }
  }

  private def applyAction(game: GameRunning, player: String): Logged[GameRunning] = {
    val actions = game.robotActions(player)
    val action = actions.head
    for {
      robot <- game.robots(player).log(RobotAction(player, action))
      afterAction <- (action match {
        case turn: TurnAction => turnAction(player, robot, turn, game)
        case move: MoveAction => moveAction(player, robot, move, game)
      }).map(_.copy(robotActions = game.robotActions + (player -> actions.tail)))
      afterEffects <- ScenarioEffects.afterAction(afterAction)
    } yield afterEffects
  }

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
        PushRobots(robot.position, direction, game.robots)
      else
        game.robots.log(RobotMovementBlocked(player, robot.position, direction))
    } yield game.copy(robots = updatedRobots)
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

}
