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
      gameState.log(PlayerAlreadyRegistered)
    case g@GameNotStarted(playersNames) =>
      g.copy(playersNames = playersNames :+ playerName).log(CommandAccepted(this))
    case g: GameRunning =>
      g.log(GameAlreadyRunning)
  }
}

case class StartGame(scenario: GameScenario) extends Command {
  def apply(gameState: GameState): LoggedGameState = gameState match {
    case g: GameRunning => g.log(GameAlreadyRunning)
    case GameNotStarted(playersNames) if playersNames.isEmpty => gameState.log(NoPlayersRegistered)
    case GameNotStarted(playersNames) => GameRunning(
      cycle = 0,
      players = playersNames,
      scenario = scenario,
      robots = playersNames.zipWithIndex.map { case (name, index) => name -> scenario.initialRobots(index) }.toMap,
      robotActions = Map.empty)
      .log(CommandAccepted(this))
  }
}

case class DefineNextAction(player: String, action: Action) extends Command {
  def apply(gameState: GameState): LoggedGameState = gameState match {
    case g: GameNotStarted => g.log(GameNotRunning)
    case g: GameRunning => g.copy(robotActions = g.robotActions + (player -> action)).log(CommandAccepted(this))
  }
}