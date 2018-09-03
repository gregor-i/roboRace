package gameLogic
package gameUpdate

object MoveRobots {
  def apply(player: Player, instruction: MoveInstruction, game: Game): Game = {
    def move(direction: Direction): Game =
      pushRobots(player.robot.position, direction, game) match {
        case Some(robotPushed) =>
          val afterPush = Events.move(robotPushed)(game)
          ScenarioEffects.afterMoveAction(afterPush)
        case None => game.log(MovementBlocked(player.index, player.robot))
      }

    instruction match {
      case MoveForward => move(player.robot.direction)
      case MoveBackward => move(player.robot.direction.back)
      case StepRight => move(player.robot.direction.right)
      case StepLeft => move(player.robot.direction.left)
      case MoveTwiceForward =>
        val updatedGame = move(player.robot.direction)
        val updatedPlayer = updatedGame.players.find(_.index == player.index).get
        if (updatedPlayer.instructionSlots == Instruction.emptySlots) {
          updatedGame
        } else {
          apply(updatedPlayer, MoveForward, updatedGame)
        }
    }
  }


  def pushRobots(position: Position, direction: Direction, gameRunning: Game): Option[RobotPushed] =
    gameRunning.players.find(player => player.robot.position == position && player.finished.isEmpty) match {
      case Some(player) if movementIsAllowed(gameRunning, position, direction) =>
        val nextPos = direction(position)
        val rec = pushRobots(nextPos, direction, gameRunning)
        Some(RobotPushed(player, nextPos, rec))
      case _ => None
    }


  private def movementIsAllowed(game: Game, position: Position, direction: Direction): Boolean = {
    val walls = game.scenario.walls
    val blockedByWall = direction match {
      case Up => walls.contains(Wall(Up(position), Down))
      case UpRight => walls.contains(Wall(position, UpRight))
      case DownRight => walls.contains(Wall(position, DownRight))
      case Down => walls.contains(Wall(position, Down))
      case DownLeft => walls.contains(Wall(DownLeft(position), UpRight))
      case UpLeft => walls.contains(Wall(UpLeft(position), DownRight))
    }
    if (blockedByWall)
      false
    else if (game.players.exists(player => player.robot.position == direction(position) && player.finished.isEmpty))
      movementIsAllowed(game, direction(position), direction)
    else
      true
  }
}
