package gameLogic
package gameUpdate

object Cycle{
  def apply(gameState: GameState): Logged[GameState] = gameState match {
    case g: GameStarting if g.players.forall(_.ready) =>
      GameRunning(
        cycle = 0,
        players = g.players.map(player => RunningPlayer(player.index, player.name, g.scenario.initialRobots(player.index), Seq.empty, None, DealOptions())),
        scenario = g.scenario).log(GameStarted())

    case g: GameRunning if g.players.forall(player => player.finished.isDefined || player.actions.size == Constants.actionsPerCycle) =>
      for {
        _ <- ().log(AllPlayerDefinedActions)
        afterPlayerActions <- execAllActions(g)
        afterEffects <- ScenarioEffects.afterCycle(afterPlayerActions)
        nextState <- afterEffects match {
          case running if running.players.forall(_.finished.isDefined) =>
            GameFinished(running.players, scenario = running.scenario).log(AllPlayersFinished)
          case running => afterEffects.copy(cycle = g.cycle + 1, players = afterEffects.players.map(_.copy(possibleActions = DealOptions())))
            .log(PlayerActionsExecuted(g.cycle + 1))
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

  private def calcNextPlayer(gameState: GameRunning): Logged[Option[RunningPlayer]] = {
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

    def nextPlayerWeight(player: RunningPlayer): (Int, Double, Double) = {
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

  private def applyAction(game: GameRunning, player: RunningPlayer): Logged[GameRunning] = {
    val action = player.actions.head
    for {
      robot <- player.robot.log(RobotAction(player.name, action))
      afterAction <- action match {
        case turn: TurnAction => turnAction(player, turn, game)
        case move: MoveAction => MoveRobots(player, move, game)
        case Sleep => sleepAction(player, game)
      }
      afterRemovedAction = afterAction.copy(players = afterAction.players.map(p => if (p.name == player.name) p.copy(actions = p.actions.drop(1)) else p))
    } yield afterRemovedAction
  }

  private def turnAction(player: RunningPlayer, action: TurnAction, game: GameRunning): Logged[GameRunning] = {
    val nextDirection = action match {
      case TurnLeft => player.robot.direction.left
      case TurnRight => player.robot.direction.right
      case UTurn => player.robot.direction.right.right
    }
    for {
      nextRobotState <- player.robot.copy(direction = nextDirection).log(RobotDirectionTransition(player.name, player.robot.direction, nextDirection))
    } yield game.copy(players = game.players.map(p => if(p.name == player.name) p.copy(robot = nextRobotState) else p))
  }

  private def sleepAction(player: RunningPlayer, game: GameRunning): Logged[GameRunning] = Logged.pure(game)
}
