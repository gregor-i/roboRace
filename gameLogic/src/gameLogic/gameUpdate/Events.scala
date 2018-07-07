package gameLogic
package gameUpdate

import Robot._

case class RobotPushed(player: Player, to: Position, push: Option[RobotPushed])

object Events {
  private def robot(playerName: String) = Game.player(playerName) composeLens Player.robot

  def move(event: RobotPushed)(game: Game): Game = {
    val pushed = (robot(event.player.name) composeLens position)
      .set(event.to)(game)
      .addLogs(RobotMoves(event.player.name, event.to, event.push))
    event.push match {
      case Some(rec) => move(rec)(pushed)
      case None => pushed
    }
  }

  def turn(player: Player, nextDirection: Direction)(game: Game): Game =
    (robot(player.name) composeLens direction)
      .set(nextDirection)(game)
      .addLogs(RobotTurns(player.name, nextDirection))

  def reset(player: Player, initialRobot: Robot)(game: Game): Game =
    robot(player.name).set(initialRobot)
      .andThen((Game.player(player.name) composeLens Player.instructionSlots).set(Instruction.emptySlots))
      .apply(game)
      .addLogs(RobotReset(player.name, initialRobot, game.cycle))

}
