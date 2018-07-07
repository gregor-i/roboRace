package gameLogic
package gameUpdate

import monocle.function.Each.each

object Cycle{
  def apply(gameState: Game): Game = gameState match {
    case g: Game if g.players.forall(player => player.finished.isDefined || player.instructionSlots.flatten.size == Constants.instructionsPerCycle) =>
      val afterPlayerActions = execAllActions(g)
      val afterEffects = ScenarioEffects.afterCycle(afterPlayerActions)
      val o1 = Game.players composeTraversal each composeLens Player.instructionOptions set DealOptions()
      val o2 = Game.cycle modify (_ + 1)
      o1.andThen(o2)(afterEffects)
    case _ => gameState
  }

  private def execAllActions(gameRunning: Game): Game =
    calcNextPlayer(gameRunning) match {
      case Some(nextPlayer) =>
        execAllActions(applyAction(gameRunning, nextPlayer))
      case None => gameRunning
    }

  private def calcNextPlayer(gameState: Game): Option[Player] = {
    val beacon = gameState.scenario.beaconPosition

    def nextPlayerWeight(player: Player): (Int, Double, Double) = {
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

  private def applyAction(game: Game, player: Player): Game = {
    val slot = player.instructionSlots.indexWhere(_.isDefined)
    val instruction = player.instructionOptions(player.instructionSlots(slot).get)
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
