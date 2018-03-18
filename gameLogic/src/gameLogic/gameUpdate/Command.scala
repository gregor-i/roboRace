package gameLogic
package gameUpdate

import gameLogic.action.{Action, ActionSlots}

sealed trait Command {
  def apply(gameState: GameState): Logged[GameState]
}

sealed trait FoldingCommand extends Command {
  def apply(gameState: GameState): Logged[GameState] = gameState.fold(ifNotDefined)(ifNotStarted)(ifRunning)(ifFinished)

  def ifNotDefined: GameNotDefined.type => Logged[GameState] = _.log(rejected(WrongState))
  def ifNotStarted: GameNotStarted => Logged[GameState] = _.log(rejected(WrongState))
  def ifRunning: GameRunning => Logged[GameState] = _.log(rejected(WrongState))
  def ifFinished: GameFinished => Logged[GameState] = _.log(rejected(WrongState))

  final def rejected(rejectionReason: RejectionReason): CommandRejected = CommandRejected(this, rejectionReason)
  final def accepted: CommandAccepted = CommandAccepted(this)
}

case class DefineScenario(scenario: GameScenario) extends FoldingCommand{
  override def ifNotDefined: GameNotDefined.type => Logged[GameState] =
    _ => GameNotStarted(scenario, Nil).log(CommandAccepted(this))
}

case class RegisterForGame(playerName: String) extends FoldingCommand {
  override def ifNotStarted: GameNotStarted => Logged[GameState] = {
    case g if g.playerNames.contains(playerName) =>
      g.log(rejected(PlayerAlreadyRegistered))
    case g if g.playerNames.size >= g.scenario.initialRobots.size =>
      g.log(rejected(TooMuchPlayersRegistered))
    case g =>
      g.copy(playerNames = g.playerNames :+ playerName).log(accepted)
  }
}

case object StartGame extends FoldingCommand {
  override def ifNotStarted: GameNotStarted => Logged[GameState] = {
    case g if g.playerNames.isEmpty => g.log(rejected(NoPlayersRegistered))
    case g => GameRunning(
      cycle = 0,
      players = g.playerNames,
      finishedPlayers = Seq.empty,
      scenario = g.scenario,
      robots = g.playerNames.zipWithIndex.map { case (name, index) => name -> g.scenario.initialRobots(index) }.toMap,
      robotActions = Map.empty)
      .log(accepted)
  }
}

case class DefineNextAction(player: String, cycle: Int, slot: Int, action: Option[Action]) extends FoldingCommand {
  override def ifRunning: GameRunning => Logged[GameRunning] = {
    case g if g.cycle != cycle => g.log(rejected(WrongCycle))
    case g if !g.players.contains(player) => g.log(rejected(PlayerNotFound))
    case g if slot < 0 || slot >= ActionSlots.actionsPerCycle => g.log(rejected(InvalidActionSlot))
    case g => g.copy(robotActions = g.robotActions + (player -> g.robotActions.getOrElse(player, ActionSlots.emptyActionSet).updated(slot, action))).log(accepted)
  }
}
