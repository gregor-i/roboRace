package gameLogic
package gameUpdate

import gameLogic.action.Action

sealed trait Command extends GameUpdate {
  def apply(gameState: GameState): LoggedGameState
}

sealed trait FoldingCommand extends Command {
  def apply(gameState: GameState): LoggedGameState = gameState.fold(ifNotDefined)(ifNotStarted)(ifRunning)(ifFinished)

  def ifNotDefined: GameNotDefined.type => LoggedGameState = _.log(CommandRejected(this, WrongState))
  def ifNotStarted: GameNotStarted => LoggedGameState = _.log(CommandRejected(this, WrongState))
  def ifRunning: GameRunning => LoggedGameState = _.log(CommandRejected(this, WrongState))
  def ifFinished: GameFinished => LoggedGameState = _.log(CommandRejected(this, WrongState))
}

case class DefineScenario(scenario: GameScenario) extends FoldingCommand{
  override def ifNotDefined: GameNotDefined.type => LoggedGameState =
    _ => GameNotStarted(scenario, Nil).log(CommandAccepted(this))
}

case class RegisterForGame(playerName: String) extends FoldingCommand {
  override def ifNotStarted: GameNotStarted => LoggedGameState = {
    case g if g.playerNames.contains(playerName) =>
      g.log(CommandRejected(this, PlayerAlreadyRegistered))
    case g if g.playerNames.size >= g.scenario.initialRobots.size =>
      g.log(CommandRejected(this, TooMuchPlayersRegistered))
    case g =>
      g.copy(playerNames = g.playerNames :+ playerName).log(CommandAccepted(this))
  }
}

case object StartGame extends FoldingCommand {
  override def ifNotStarted: GameNotStarted => LoggedGameState = {
    case g if g.playerNames.isEmpty => g.log(CommandRejected(this, NoPlayersRegistered))
    case g => GameRunning(
      cycle = 0,
      players = g.playerNames,
      scenario = g.scenario,
      robots = g.playerNames.zipWithIndex.map { case (name, index) => name -> g.scenario.initialRobots(index) }.toMap,
      robotActions = Map.empty)
      .log(CommandAccepted(this))
  }
}

case class DefineNextAction(player: String, cycle: Int, action: Action) extends FoldingCommand {
  override def ifRunning: GameRunning => LoggedGameState = {
    case g: GameRunning if g.cycle != cycle => g.log(CommandRejected(this, WrongCycle))
    case g: GameRunning => g.copy(robotActions = g.robotActions + (player -> action)).log(CommandAccepted(this))
  }
}