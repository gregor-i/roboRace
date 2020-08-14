package logic
package gameUpdate

import entities._

case class RobotPushed(player: RunningPlayer, to: Position, push: Option[RobotPushed])

object Events {
  private def asEvent(pushed: RobotPushed): RobotMoves = {
    def loop(event: RobotPushed): List[RobotPositionTransition] = {
      val head = RobotPositionTransition(event.player.index, event.player.robot.direction, event.player.robot.position, event.to)
      event.push.fold(head :: Nil)(head :: loop(_))
    }

    RobotMoves(loop(pushed))
  }

  def move(event: RobotPushed)(game: Game): Game = {
    def loop(event: RobotPushed, game: Game): Game = {
      val pushed = Lenses.position(event.player.id).set(event.to)(game)
      event.push match {
        case Some(rec) => loop(rec, pushed)
        case None      => pushed
      }
    }
    TrapEffects.afterMove(event)(loop(event, game).log(asEvent(event)))
  }

  def turn(player: RunningPlayer, nextDirection: Direction): Game => Game =
    State.sequence(
      Lenses.direction(player.id).set(nextDirection),
      Lenses.log(RobotTurns(player.index, player.robot.position, player.robot.direction, nextDirection))
    )

  def reset(player: RunningPlayer, initialRobot: Robot): Game => Game =
    State.sequence(
      Lenses.robot(player.id).set(initialRobot),
      Lenses.instructionSlots(player.id).set(Seq.empty),
      Lenses.log(RobotReset(player.index, player.robot, initialRobot))
    )

  def stun(player: RunningPlayer): Game => Game = game => {
    Lenses.instructionSlots(player.id).modify(_.drop(1))(game)
  }
}
