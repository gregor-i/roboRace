package gameLogic
package command

import gameLogic.action.Action
import gameLogic.eventLog._

sealed trait Command extends GameUpdate {
  def apply(gameState: GameState): LoggedGameState
}

case class RegisterForGame(playerName: String) extends Command {
  def apply(gameState: GameState): LoggedGameState = gameState match {
    case GameNotStarted(playersNames) if playersNames.contains(playerName) =>
      gameState.log(CommandRejected(this, PlayerAlreadyRegistered))
    case g@GameNotStarted(playersNames) =>
      g.copy(playersNames = playersNames :+ playerName).log(CommandAccepted(this))
    case g: GameRunning =>
      g.log(CommandRejected(this, GameAlreadyRunning))
  }
}

case class StartGame(scenario: GameScenario) extends Command {
  def apply(gameState: GameState): LoggedGameState = gameState match {
    case g: GameRunning => g.log(CommandRejected(this, GameAlreadyRunning))
    case GameNotStarted(playersNames) if playersNames.isEmpty => gameState.log(CommandRejected(this, NoPlayersRegistered))
    case GameNotStarted(playersNames) => GameRunning(
      cycle = 0,
      players = playersNames,
      scenario = scenario,
      robots = playersNames.zipWithIndex.map { case (name, index) => name -> scenario.initialRobots(index) }.toMap,
      robotActions = Map.empty)
      .log(CommandAccepted(this))
  }
}

case class DefineNextAction(player: String, cycle: Int, action: Action) extends Command {
  def apply(gameState: GameState): LoggedGameState = gameState match {
    case g: GameNotStarted => g.log(CommandRejected(this, GameNotRunning))
    case g: GameRunning if g.cycle != cycle => g.log(CommandRejected(this, WrongCycle))
    case g: GameRunning => g.copy(robotActions = g.robotActions + (player -> action)).log(CommandAccepted(this))
  }
}