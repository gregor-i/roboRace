package gameLogic
package gameUpdate

import gameEntities._

object MoveRobots {
  def apply(player: Player, instruction: MoveInstruction, game: Game): Game = {
    def move(direction: Direction): Game =
      pushRobots(player.robot.position, direction, game) match {
        case Some(robotPushed) =>
          val afterPush = Events.move(robotPushed)(game)
          ScenarioEffects.afterMoveAction(afterPush)
        case None => Lenses.log(MovementBlocked(player.index, player.robot))(game)
      }

    instruction match {
      case MoveForward => move(player.robot.direction)
      case MoveBackward => move(Direction.back(player.robot.direction))
      case StepRight => move(Direction.turnRight(player.robot.direction))
      case StepLeft => move(Direction.turnLeft(player.robot.direction))
      case MoveTwiceForward =>
        val updatedGame = move(player.robot.direction)
        val updatedPlayer = updatedGame.players.find(_.index == player.index).get
        if (updatedPlayer.instructionSlots.isEmpty) { // player has been resetted
          updatedGame
        } else {
          apply(updatedPlayer, MoveForward, updatedGame)
        }
    }
  }


  def pushRobots(position: Position, direction: Direction, gameRunning: Game): Option[RobotPushed] =
    gameRunning.players.find(player => player.robot.position == position && player.finished.isEmpty) match {
      case Some(player) if movementIsAllowed(gameRunning, position, direction) =>
        val nextPos = Direction.move(direction, position)
        val rec = pushRobots(nextPos, direction, gameRunning)
        Some(RobotPushed(player, nextPos, rec))
      case _ => None
    }


  private def movementIsAllowed(game: Game, position: Position, direction: Direction): Boolean = {
    val walls = game.scenario.walls
    val blockedByWall = direction match {
      case w: WallDirection => walls.contains(Wall(position, w))
      case Up               => walls.contains(Wall(Direction.move(direction, position), Down))
      case DownLeft         => walls.contains(Wall(Direction.move(direction, position), UpRight))
      case UpLeft           => walls.contains(Wall(Direction.move(direction, position), DownRight))
    }
    if (blockedByWall)
      false
    else if (game.players.exists(player => player.robot.position == Direction.move(direction, position) && player.finished.isEmpty))
      movementIsAllowed(game, Direction.move(direction, position), direction)
    else
      true
  }
}
