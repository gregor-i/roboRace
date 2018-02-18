package gameLogic
package command

import gameLogic.eventLog._

sealed trait Command extends (GameState => GameLogged[GameState]){
  def apply(gameState: GameState): GameLogged[GameState]
}

case class RegisterForGame(playerName: String) extends Command {
  def apply(gameState: GameState): GameLogged[GameState] = gameState match {
    case GameNotStarted(playersNames) if playersNames.contains(playerName) =>
      gameState.log(PlayerAlreadyRegistered)
    case g@GameNotStarted(playersNames) =>
      g.copy(playersNames = playersNames :+ playerName).log(PlayerRegisteredForGame(playerName))
    case g: GameRunning =>
      g.log(GameAlreadyRunning)
  }
}

case class StartGame(gameMap: GameMap) extends Command {
  def apply(gameState: GameState): GameLogged[GameState] = gameState match {
    case GameNotStarted(playersNames) if playersNames.isEmpty => gameState.log(NoPlayersRegistered)
    case GameNotStarted(playersNames) => GameRunning(cycle = 0, gameMap = gameMap).log(GameStarted)
    case g: GameRunning => g.log(GameAlreadyRunning)
  }
}