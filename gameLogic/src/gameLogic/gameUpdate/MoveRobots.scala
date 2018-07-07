package gameLogic
package gameUpdate

object MoveRobots {
  def apply(player: Player, instruction: MoveInstruction, game: Game): Game = {
    def move(position: Position, direction: Direction, gameRunning: Game): Game =
      pushRobots(position, direction, gameRunning) match {
        case Some(robotPushed) =>
          val afterPush = Events.move(robotPushed)(game)
          ScenarioEffects.afterMoveAction(afterPush)
        case None => gameRunning.addLogs(MovementBlocked(player.name, player.robot.position, player.robot.direction))
      }

    instruction match {
      case MoveForward => move(player.robot.position, player.robot.direction, game)
      case MoveBackward => move(player.robot.position, player.robot.direction.back, game)
      case StepRight => move(player.robot.position, player.robot.direction.right, game)
      case StepLeft => move(player.robot.position, player.robot.direction.left, game)
      case MoveTwiceForward =>
        val after1Move = move(player.robot.position, player.robot.direction, game)
        val fallen = after1Move.events.drop(game.cycle-1).headOption.getOrElse(Seq.empty).collect{ case RobotReset(player.name, _) => true }.nonEmpty // todo: I need a better solution.
        if(fallen) {
          after1Move
        }else{
          move(after1Move.players.find(_.name == player.name).get.robot.position, player.robot.direction, after1Move)
        }
    }
  }


  def pushRobots(position: Position, direction: Direction, gameRunning: Game): Option[RobotPushed] =
    gameRunning.players.find(_.robot.position == position) match {
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
    //        if (game.scenario.beaconPosition == position.move(direction))
    //          false
    else if (game.players.exists(player => player.robot.position == direction(position) && player.finished.isEmpty))
      movementIsAllowed(game, direction(position), direction)
    else
      true
  }
}
