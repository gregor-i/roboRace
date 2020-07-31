package gameLogic
package gameUpdate

import gameEntities._

object Cycle extends (Game => Game) {
  def apply(game: Game): Game = {
    def readyForCycle(g: Game): Boolean =
      Lenses.runningPlayers.getAll(g).forall(_.instructionSlots.nonEmpty)

    State.conditional(readyForCycle)(
      State.sequence(
        removeUsedOptions,
        g => Lenses.log(StartCycleEvaluation(g.cycle))(g),
        ScenarioEffects.beforeCycle,
        execAllActions,
        ScenarioEffects.afterCycle,
        g => Lenses.log(FinishedCycleEvaluation(g.cycle))(g),
        Lenses.runningPlayers composeLens RunningPlayer.instructionOptions modify DealOptions.apply,
        Game.cycle.modify(_ + 1),
        State.conditional(Lenses.runningPlayers.isEmpty)(
          Lenses.log(AllPlayersFinished)
        )
      )
    )(game)
  }

  private def removeUsedOptions: Game => Game =
    Lenses.runningPlayers.modify { player =>
      val usedOptions = player.instructionSlots
      player.copy(
        instructionOptions = player.instructionOptions
          .map(option => option.copy(count = option.count - usedOptions.count(_ == option.instruction)))
      )
    }

  private def execAllActions(game: Game): Game =
    calcNextPlayer(game) match {
      case Some(nextPlayer) => execAllActions(applyAction(game, nextPlayer))
      case None             => game
    }

  private def calcNextPlayer(gameState: Game): Option[RunningPlayer] = {
    def nextPlayerWeight(player: RunningPlayer): (Int, Double, Double) = {
      val position = player.robot.position
      val target   = gameState.scenario.targets(player.currentTarget)

      val dx       = position.x - target.x
      val dy       = position.y - target.y
      val distance = Math.sqrt(dx * dx + dy * dy)
      val angle    = Math.atan2(dx, dy)
      (player.instructionSlots.size, distance, angle)
    }

    if (Lenses.runningPlayers.getAll(gameState).forall(_.instructionSlots.isEmpty)) {
      None
    } else {
      Some(Lenses.runningPlayers.getAll(gameState).maxBy(nextPlayerWeight))
    }
  }

  private def applyAction(_game: Game, player: RunningPlayer): Game = {
    val instruction = player.instructionSlots.head
    val game        = Lenses.log(RobotAction(player.index, instruction))(_game)
    val afterInstruction = instruction match {
      case TurnRight => Events.turn(player, Direction.turnRight(player.robot.direction))(game)
      case TurnLeft  => Events.turn(player, Direction.turnLeft(player.robot.direction))(game)
      case UTurn     => Events.turn(player, Direction.back(player.robot.direction))(game)

      case move: MoveInstruction => MoveRobots(player, move, game)

      case Sleep => game
    }
    Lenses.instructionSlots(player.id).modify(_.drop(1))(afterInstruction)
  }
}
