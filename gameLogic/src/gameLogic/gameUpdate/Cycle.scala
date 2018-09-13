package gameLogic
package gameUpdate

import monocle.function.Each.each

object Cycle extends (Game => Game){
  def apply(game: Game): Game = {
    def readyForCycle(g: Game): Boolean =
      g.players.forall(p => p.finished.isDefined || p.instructionSlots.flatten.size == Constants.instructionsPerCycle) && !g.players.forall(_.finished.isDefined)

    State.sequence(
      State.conditional(readyForCycle)(
        State.sequence(
          g => g.log(StartCycleEvaluation(g.cycle)),
          ScenarioEffects.beforeCycle,
          execAllActions,
          ScenarioEffects.afterCycle,
          g => g.log(FinishedCycleEvaluation(g.cycle)),
          Game.players composeTraversal each composeLens Player.instructionOptions set DealOptions(),
          Game.cycle modify (_ + 1),
          State.conditional(_.players.forall(_.finished.isDefined))(
            _.log(AllPlayersFinished)
          )
        )
      )
    )(game)
  }

  private def execAllActions(gameRunning: Game): Game =
    calcNextPlayer(gameRunning) match {
      case Some(nextPlayer) => execAllActions(applyAction(gameRunning, nextPlayer))
      case None => gameRunning
    }

  private def calcNextPlayer(gameState: Game): Option[Player] = {
    def nextPlayerWeight(player: Player): (Int, Double, Double) = {
      val position = player.robot.position
      val dx = position.x - gameState.scenario.targetPosition.x
      val dy = position.y - gameState.scenario.targetPosition.y
      val distance = Math.sqrt(dx * dx + dy * dy)
      val angle = Math.atan2(dx, dy)
      (player.instructionSlots.flatten.size, distance, angle)
    }

    if (gameState.players.forall(_.instructionSlots.flatten.isEmpty)) {
       None
    } else {
      Some(gameState.players.maxBy(nextPlayerWeight))
    }
  }

  private def applyAction(_game: Game, player: Player): Game = {
    val slot = player.instructionSlots.indexWhere(_.isDefined)
    val instruction = player.instructionOptions(player.instructionSlots(slot).get)
    val game = _game.log(RobotAction(player.index, instruction))
    val afterInstruction = instruction match {
      case TurnRight => Events.turn(player, player.robot.direction.right)(game)
      case TurnLeft => Events.turn(player, player.robot.direction.left)(game)
      case UTurn => Events.turn(player, player.robot.direction.back)(game)

      case move: MoveInstruction => MoveRobots(player, move, game)

      case Sleep => game
    }
    (Game.player(player.name) composeLens Player.instructionSlots).modify(_.updated(slot, None))(afterInstruction)
  }
}
