package gameLogic
package gameUpdate

import gameLogic.action.{Action, ActionSlots}

sealed trait CommandResponse
case class CommandRejected(reason: RejectionReason, command:Command) extends CommandResponse
case class CommandAccepted(newState: GameState) extends CommandResponse

sealed trait Command {
  type R = CommandResponse
  def apply(gameState: GameState): R
}

sealed trait FoldingCommand extends Command {
  def apply(gameState: GameState): R = gameState.fold(ifNotDefined)(ifNotStarted)(ifRunning)(ifFinished)

  def ifNotDefined: GameNotDefined.type => R = _ => rejected(WrongState)
  def ifNotStarted: GameNotStarted => R = _ => rejected(WrongState)
  def ifRunning: GameRunning => R = _ => rejected(WrongState)
  def ifFinished: GameFinished => R = _ => rejected(WrongState)

  final def rejected(rejectionReason: RejectionReason): R = CommandRejected(rejectionReason, this)
  final def accepted(s: GameState): R = CommandAccepted(s)
}

case class DefineScenario(scenario: GameScenario) extends FoldingCommand{
  override def ifNotDefined: GameNotDefined.type => R =
    _ => accepted(GameNotStarted(scenario, Nil))
}

case class RegisterForGame(playerName: String) extends FoldingCommand {
  override def ifNotStarted: GameNotStarted => R = {
    case g if g.playerNames.contains(playerName) =>
      rejected(PlayerAlreadyRegistered)
    case g if g.playerNames.size >= g.scenario.initialRobots.size =>
      rejected(TooMuchPlayersRegistered)
    case g =>
      accepted(g.copy(playerNames = g.playerNames :+ playerName))
  }
}

case object StartGame extends FoldingCommand {
  override def ifNotStarted: GameNotStarted => R = {
    case g if g.playerNames.isEmpty => rejected(NoPlayersRegistered)
    case g => accepted(GameRunning(
      cycle = 0,
      players = g.playerNames,
      finishedPlayers = Seq.empty,
      scenario = g.scenario,
      robots = g.playerNames.zipWithIndex.map { case (name, index) => name -> g.scenario.initialRobots(index) }.toMap,
      robotActions = Map.empty))
  }
}

case class DefineNextAction(player: String, cycle: Int, actions: Seq[Action]) extends FoldingCommand {
  override def ifRunning: GameRunning => R = {
    case g if g.cycle != cycle => rejected(WrongCycle)
    case g if !g.players.contains(player) => rejected(PlayerNotFound)
    case g if actions.size != ActionSlots.actionsPerCycle => rejected(InvalidActionCount)
    case g => accepted(
      g.copy(robotActions = g.robotActions + (player -> actions))
    )
  }
}
