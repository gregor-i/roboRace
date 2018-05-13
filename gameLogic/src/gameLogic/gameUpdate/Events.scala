package gameLogic
package gameUpdate

import Robot._

object Events {
  private def robot(playerName: String) = GameRunning.player(playerName) composeLens RunningPlayer.robot

  def move(player: RunningPlayer, nextPos: Position)(game: GameRunning): Logged[GameRunning] =
    (robot(player.name) composeLens position)
      .set(nextPos)(game)
      .log(RobotMoves(player.name, nextPos))

  def turn(player: RunningPlayer, nextDirection: Direction)(game: GameRunning): Logged[GameRunning] =
    (robot(player.name) composeLens direction)
      .set(nextDirection)(game)
      .log(RobotTurns(player.name, nextDirection))

  def reset(player: RunningPlayer, initialRobot: Robot)(game: GameRunning): Logged[GameRunning] =
    robot(player.name).set(initialRobot)
      .andThen((GameRunning.player(player.name) composeLens RunningPlayer.actions).set(List.empty))
      .apply(game)
      .log(RobotReset(player.name, initialRobot))

}
