package gameLogic
package gameUpdate

object Cycle{
  def apply(gameState: GameState): Logged[GameState] = gameState match {
    case g: GameRunning if g.players.forall(player => player.finished.isDefined || player.actions.size == Constants.actionsPerCycle) =>
      for {
        _ <- ().log(AllPlayerDefinedActions)
        afterPlayerActions <- execAllActions(g)
        afterEffects <- ScenarioEffects.afterCycle(afterPlayerActions)
        nextState <- afterEffects match {
          case running if running.players.forall(_.finished.isDefined) =>
            GameFinished(running.players, scenario = running.scenario).log(AllPlayersFinished)
          case running => afterEffects.copy(cycle = g.cycle + 1).log(PlayerActionsExecuted(g.cycle + 1))
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

  private def calcNextPlayer(gameState: GameRunning): Logged[Option[Player]] = {
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

    def nextPlayerWeight(player: Player): (Int, Double, Double) = {
      val position = player.robot.position
      (Constants.actionsPerCycle - player.actions.size, distance(position), angle(position))
    }

    if (gameState.players.forall(_.actions.isEmpty)) {
       None.log()
    } else {
      val p = gameState.players.minBy(nextPlayerWeight)
      Some(p).log(NextRobotForActionDefined(p.name))
    }
  }

  private def applyAction(game: GameRunning, player: Player): Logged[GameRunning] = {
    val action = player.actions.head
    for {
      robot <- player.robot.log(RobotAction(player.name, action))
      afterAction <- action match {
        case turn: TurnAction => turnAction(player, robot, turn, game)
        case move: MoveAction => moveAction(player, robot, move, game)
      }
      afterDroppedAction = afterAction.copy(players = afterAction.players.map(p => if(p.name == player.name) p.copy(actions = p.actions.tail) else p))
      afterEffects <- ScenarioEffects.afterAction(afterDroppedAction)
    } yield afterEffects
  }

  private def turnAction(player: Player, robot: Robot, action: TurnAction, game: GameRunning): Logged[GameRunning] = {
    val nextDirection = action match {
      case TurnLeft => robot.direction.left
      case TurnRight => robot.direction.right
    }
    for {
      nextRobotState <- robot.copy(direction = nextDirection).log(RobotDirectionTransition(player.name, robot.direction, nextDirection))
    } yield game.copy(players = game.players.map(p => if(p.name == player.name) p.copy(robot = nextRobotState) else p))
  }

  private def moveAction(player: Player, robot: Robot, action: MoveAction, game: GameRunning): Logged[GameRunning] = {
    val direction = action match {
      case MoveForward => robot.direction
      case MoveBackward => robot.direction.back
    }

    if (movementIsAllowed(game, robot.position, direction))
      PushRobots(robot.position, direction, game)
    else
      game.log(RobotMovementBlocked(player.name, robot.position, direction))
  }


  private def movementIsAllowed(game: GameRunning, position: Position, direction: Direction): Boolean = {
    val downWalls = game.scenario.walls.filter(_.direction == Down).map(_.position)
    val rightWalls = game.scenario.walls.filter(_.direction == Right).map(_.position)
//    if (game.scenario.beaconPosition == position.move(direction))
//      false
    if (direction == Down && downWalls.contains(position))
      false
    else if (direction == Right && rightWalls.contains(position))
      false
    else if (direction == Up && downWalls.contains(position.move(Up)))
      false
    else if (direction == Left && rightWalls.contains(position.move(Left)))
      false
    else if (game.players.exists(_.robot.position == position.move(direction)))
      movementIsAllowed(game, position.move(direction), direction)
    else
      true
  }

}
