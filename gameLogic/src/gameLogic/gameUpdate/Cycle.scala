package gameLogic
package gameUpdate

import monocle.function.Each.each

object Cycle{
  def apply(gameState: GameState): Logged[GameState] = gameState match {
    case g: GameStarting if g.players.forall(_.ready) && g.players.nonEmpty =>
      GameRunning(
        cycle = 0,
        players = g.players.map(player => RunningPlayer(player.index, player.name, g.scenario.initialRobots(player.index), Seq.empty, DealOptions.initial, None)),
        scenario = g.scenario).log(GameStarted())

    case g: GameRunning if g.players.forall(player => player.finished.isDefined || player.instructions.size == Constants.instructionsPerCycle) =>
      for {
        afterPlayerActions <- execAllActions(g)
        afterEffects <- ScenarioEffects.afterCycle(afterPlayerActions)
        nextState <- afterEffects match {
          case running if running.players.forall(_.finished.isDefined) =>
            GameFinished(running.players, scenario = running.scenario).log(AllPlayersFinished)
          case running =>
            val o1 = GameRunning.players composeTraversal each composeLens RunningPlayer.instructionOptions set DealOptions()
            val o2 = GameRunning.cycle modify (_ + 1)
            o1.andThen(o2)(running).log(StartNextCycle(running.cycle + 1))
        }
      } yield nextState

    case _ => Logged.pure(gameState)
  }

  private def execAllActions(gameRunning: GameRunning): Logged[GameRunning] =
    calcNextPlayer(gameRunning) match {
      case Some(nextPlayer) => for {
        afterAction <- applyAction(gameRunning, nextPlayer)
        afterRecursion <- execAllActions(afterAction)
      } yield afterRecursion
      case None => Logged.pure(gameRunning)
    }

  private def calcNextPlayer(gameState: GameRunning): Option[RunningPlayer] = {
    val beacon = gameState.scenario.beaconPosition

    def nextPlayerWeight(player: RunningPlayer): (Int, Double, Double) = {
      val position = player.robot.position
      val dx = position.x - beacon.x
      val dy = position.y - beacon.y
      val distance = Math.sqrt(dx * dx + dy * dy)
      val angle = Math.atan2(dx, dy)
      (-player.instructions.size, distance, angle)
    }

    if (gameState.players.forall(_.instructions.isEmpty)) {
       None
    } else {
      Some(gameState.players.minBy(nextPlayerWeight))
    }
  }

  private def applyAction(game: GameRunning, player: RunningPlayer): Logged[GameRunning] = {
    val instruction = player.instructions.head
    for {
      _ <- ().log(RobotAction(player.name, instruction))
      afterInstruction <- instruction match {
        case TurnRight => Events.turn(player, player.robot.direction.right)(game)
        case TurnLeft => Events.turn(player, player.robot.direction.left)(game)
        case UTurn => Events.turn(player, player.robot.direction.back)(game)

        case move: MoveInstruction => MoveRobots(player, move, game)

        case Sleep => Logged.pure(game)
      }
    } yield (GameRunning.player(player.name) composeLens RunningPlayer.instructions).modify(_.drop(1))(afterInstruction)
  }
}
