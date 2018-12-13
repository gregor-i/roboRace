package gameLogic
package gameUpdate

import gameEntities.{AllPlayersFinished, Constants, FinishedCycleEvaluation, Game, MoveInstruction, Player, RobotAction, Sleep, StartCycleEvaluation, TurnLeft, TurnRight, UTurn}
import monocle.function.Each.each

object Cycle extends (Game => Game){
  def apply(game: Game): Game = {
    def readyForCycle(g: Game): Boolean =
      g.players.forall(p => p.finished.isDefined || p.instructionSlots.flatten.size == Constants.instructionsPerCycle) && !g.players.forall(_.finished.isDefined)

    State.sequence(
      State.conditional(readyForCycle)(
        State.sequence(
          g => Lenses.log(StartCycleEvaluation(g.cycle))(g),
          ScenarioEffects.beforeCycle,
          execAllActions,
          ScenarioEffects.afterCycle,
          g => Lenses.log(FinishedCycleEvaluation(g.cycle))(g),
          Lenses.players composeTraversal each composeLens PlayerLenses.instructionOptions set DealOptions(),
          Lenses.cycle modify (_ + 1),
          State.conditional(_.players.forall(_.finished.isDefined))(
            Lenses.log(AllPlayersFinished)
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
    val game = Lenses.log(RobotAction(player.index, instruction))(_game)
    val afterInstruction = instruction match {
      case TurnRight => Events.turn(player, Direction.turnRight(player.robot.direction))(game)
      case TurnLeft => Events.turn(player, Direction.turnLeft(player.robot.direction))(game)
      case UTurn => Events.turn(player, Direction.back(player.robot.direction))(game)

      case move: MoveInstruction => MoveRobots(player, move, game)

      case Sleep => game
    }
    Lenses.instructionSlots(player.name).modify(_.updated(slot, None))(afterInstruction)
  }
}
