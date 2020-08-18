package logic
package gameUpdate

import entities._

import scala.annotation.tailrec

object MoveRobots {
  def apply(player: RunningPlayer, instruction: MoveInstruction, game: Game): Game = {
    def move(direction: Direction): Game =
      pushRobots(player.robot.position, direction, game) match {
        case Some(robotPushed) =>
          val afterPush = Events.move(robotPushed)(game)
          ScenarioEffects.afterMoveAction(afterPush)
        case None => game.log(MovementBlocked(player.index, player.robot))
      }

    instruction match {
      case MoveForward  => move(player.robot.direction)
      case MoveBackward => move(Direction.back(player.robot.direction))
      case StepRight    => move(Direction.turnRight(player.robot.direction))
      case StepLeft     => move(Direction.turnLeft(player.robot.direction))
      case MoveTwiceForward =>
        val updatedGame   = move(player.robot.direction)
        val updatedPlayer = Lenses.runningPlayer(player.id).getAll(updatedGame).head
        if (updatedPlayer.instructionSlots.isEmpty) { // player has been resetted
          updatedGame
        } else {
          apply(updatedPlayer, MoveForward, updatedGame)
        }
    }
  }

  def pushRobots(position: Position, direction: Direction, game: Game): Option[RobotPushed] =
    Lenses.runningPlayers.getAll(game).find(_.robot.position == position) match {
      case Some(player) if movementIsAllowed(game, position, direction) =>
        val nextPos = Direction.move(direction, position)
        val rec     = pushRobots(nextPos, direction, game)
        Some(RobotPushed(player, nextPos, rec))
      case _ => None
    }

  @tailrec
  private def movementIsAllowed(game: Game, position: Position, direction: Direction): Boolean = {
    val blockedByWall = game.scenario.walls.contains(Wall(position, direction))
    if (blockedByWall)
      false
    else if (Lenses.runningPlayers.getAll(game).exists(_.robot.position == Direction.move(direction, position)))
      movementIsAllowed(game, Direction.move(direction, position), direction)
    else
      true
  }
}
