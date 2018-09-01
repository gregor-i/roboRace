package gameLogic
package gameUpdate

import Robot._

case class RobotPushed(player: Player, to: Position, push: Option[RobotPushed])

object Events {
  private def robot(playerName: String) = Game.player(playerName) composeLens Player.robot

  private def asEvent(pushed: RobotPushed): RobotMoves = {
    def loop(event: RobotPushed): List[RobotPositionTransition] = {
      val head = RobotPositionTransition(event.player.index, event.player.robot.direction, event.player.robot.position, event.to)
      event.push.fold(head :: Nil)(head :: loop(_))
    }

    RobotMoves(loop(pushed))
  }

  def move(event: RobotPushed)(game: Game): Game = {
    def loop(event: RobotPushed, game: Game) : Game = {
      val pushed = (robot(event.player.name) composeLens position)
        .set(event.to)(game)
      event.push match {
        case Some(rec) => loop(rec, pushed)
        case None => pushed
      }
    }
    loop(event, game).log(asEvent(event))
  }

  def turn(player: Player, nextDirection: Direction): Game => Game =
    State.sequence(
      (robot(player.name) composeLens direction).set(nextDirection),
      _.log(RobotTurns(player.index, player.robot.position, player.robot.direction, nextDirection))
    )

  def reset(player: Player, initialRobot: Robot): Game => Game =
    State.sequence(
      robot(player.name).set(initialRobot),
      (Game.player(player.name) composeLens Player.instructionSlots).set(Instruction.emptySlots),
      _.log(RobotReset(player.index, player.robot, initialRobot))
    )
}
