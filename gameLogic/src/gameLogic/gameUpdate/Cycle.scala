package gameLogic
package gameUpdate

import monocle.function.Each.each

object Cycle{
  def apply(gameState: Game): Logged[Game] = gameState match {
    case g: Game if g.players.forall(player => player.finished.isDefined || player.instructionSlots.flatten.size == Constants.instructionsPerCycle) =>
      for {
        afterPlayerActions <- execAllActions(g)
        afterEffects <- ScenarioEffects.afterCycle(afterPlayerActions)
        nextState <- afterEffects match {
          case running =>
            val o1 = Game.players composeTraversal each composeLens RunningPlayer.instructionOptions set DealOptions()
            val o2 = Game.cycle modify (_ + 1)
            o1.andThen(o2)(running).log(StartNextCycle(running.cycle + 1))
        }
      } yield nextState

    case _ => Logged.pure(gameState)
  }

  private def execAllActions(gameRunning: Game): Logged[Game] =
    calcNextPlayer(gameRunning) match {
      case Some(nextPlayer) => for {
        afterAction <- applyAction(gameRunning, nextPlayer)
        afterRecursion <- execAllActions(afterAction)
      } yield afterRecursion
      case None => Logged.pure(gameRunning)
    }

  private def calcNextPlayer(gameState: Game): Option[RunningPlayer] = {
    val beacon = gameState.scenario.beaconPosition

    def nextPlayerWeight(player: RunningPlayer): (Int, Double, Double) = {
      val position = player.robot.position
      val dx = position.x - beacon.x
      val dy = position.y - beacon.y
      val distance = Math.sqrt(dx * dx + dy * dy)
      val angle = Math.atan2(dx, dy)
      (-player.instructionSlots.flatten.size, distance, angle)
    }

    if (gameState.players.forall(_.instructionSlots.flatten.isEmpty)) {
       None
    } else {
      Some(gameState.players.minBy(nextPlayerWeight))
    }
  }

  private def applyAction(game: Game, player: RunningPlayer): Logged[Game] = {
    val slot = player.instructionSlots.indexWhere(_.isDefined)
    val instruction = player.instructionOptions(player.instructionSlots(slot).get)
    for {
      _ <- ().log(RobotAction(player.name, instruction))
      afterInstruction <- instruction match {
        case TurnRight => Events.turn(player, player.robot.direction.right)(game)
        case TurnLeft => Events.turn(player, player.robot.direction.left)(game)
        case UTurn => Events.turn(player, player.robot.direction.back)(game)

        case move: MoveInstruction => MoveRobots(player, move, game)

        case Sleep => Logged.pure(game)
      }
    } yield (Game.player(player.name) composeLens RunningPlayer.instructionSlots).modify(_.updated(slot, None))(afterInstruction)
  }
}
