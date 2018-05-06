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
      players = for((name, index) <- g.playerNames.zipWithIndex)
        yield Player(index, name, g.scenario.initialRobots(index), Seq.empty, None, DealOptions()),
      scenario = g.scenario))
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
