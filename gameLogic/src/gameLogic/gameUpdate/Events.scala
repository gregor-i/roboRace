package gameLogic
package gameUpdate

import Robot._

object Events {
  private def robot(playerName: String) = Game.player(playerName) composeLens RunningPlayer.robot

  def move(player: RunningPlayer, nextPos: Position)(game: Game): Logged[Game] =
    (robot(player.name) composeLens position)
      .set(nextPos)(game)
      .log(RobotMoves(player.name, nextPos))

  def turn(player: RunningPlayer, nextDirection: Direction)(game: Game): Logged[Game] =
    (robot(player.name) composeLens direction)
      .set(nextDirection)(game)
      .log(RobotTurns(player.name, nextDirection))

  def reset(player: RunningPlayer, initialRobot: Robot)(game: Game): Logged[Game] =
    robot(player.name).set(initialRobot)
      .andThen((Game.player(player.name) composeLens RunningPlayer.instructionSlots).set(Instruction.emptySlots))
      .apply(game)
      .log(RobotReset(player.name, initialRobot))

}
