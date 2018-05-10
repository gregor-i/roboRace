package gameLogic
package gameUpdate

sealed trait CommandResponse
case class CommandRejected(reason: RejectionReason, command:Command) extends CommandResponse
case class CommandAccepted(newState: GameState) extends CommandResponse

sealed trait Command {
  type R = CommandResponse
  def apply(gameState: GameState): R
}

sealed trait FoldingCommand extends Command {
  def apply(gameState: GameState): R = gameState.fold(ifInitial)(ifStarting)(ifRunning)(ifFinished)

  def ifInitial: InitialGame.type => R = _ => rejected(WrongState)
  def ifStarting: GameStarting => R = _ => rejected(WrongState)
  def ifRunning: GameRunning => R = _ => rejected(WrongState)
  def ifFinished: GameFinished => R = _ => rejected(WrongState)

  final def rejected(rejectionReason: RejectionReason): R = CommandRejected(rejectionReason, this)
  final def accepted(s: GameState): R = CommandAccepted(s)
}

case class DefineScenario(scenario: GameScenario) extends FoldingCommand{
  override def ifInitial: InitialGame.type => R =
    _ => accepted(GameStarting(scenario, Nil))
}

case class RegisterForGame(playerName: String) extends FoldingCommand {
  override def ifStarting: GameStarting => R = {
    case g if g.players.exists(_.name == playerName) =>
      rejected(PlayerAlreadyRegistered)
    case g if g.players.size >= g.scenario.initialRobots.size =>
      rejected(TooMuchPlayersRegistered)
    case g =>
      accepted(g.copy(players = g.players :+ StartingPlayer(g.players.size, playerName, false)))
  }
}

case class ReadyForGame(playerName: String) extends FoldingCommand {
  override def ifStarting: GameStarting => R = {
    case g if !g.players.exists(_.name == playerName) => rejected(PlayerNotFound)
    case g => accepted(g.copy(players = g.players.map(player => if(player.name == playerName) player.copy(ready = true) else player)))
  }
}

case class DefineNextAction(player: String, cycle: Int, actions: Seq[Int]) extends FoldingCommand {
  override def ifRunning: GameRunning => R = {
    case g if g.cycle != cycle => rejected(WrongCycle)
    case g if !g.players.exists(_.name == player) => rejected(PlayerNotFound)
    case g if actions.size != Constants.actionsPerCycle ||
      actions.distinct.size != Constants.actionsPerCycle ||
      actions.forall(i => 0 > i || i > Constants.actionOptionsPerCycle) => rejected(InvalidActionChoice)
    case g => accepted(
      g.copy(players = g.players.map(p => if(p.name == player) p.copy(actions = actions.map(p.possibleActions)) else p))
    )
  }
}
