package gameLogic
package gameUpdate

object MoveRobots {
  def apply(player: RunningPlayer, instruction: MoveInstruction, game: Game): Game = {
    def move(direction: Direction, gameRunning: Game): Game ={
      val p = gameRunning.players.find(_.name == player.name).get
      if (movementIsAllowed(gameRunning, p.robot.position, direction)) {
        val afterPush = MoveRobots.pushRobots(p.robot.position, direction, gameRunning)
        ScenarioEffects.afterMoveAction(afterPush)
      }else{
        gameRunning.addLogs(MovementBlocked(p.name, p.robot.position, direction))
      }
    }

    instruction match {
      case MoveForward => move(player.robot.direction, game)
      case MoveBackward => move(player.robot.direction.back, game)
      case StepRight => move(player.robot.direction.right, game)
      case StepLeft => move(player.robot.direction.left, game)
      case MoveTwiceForward =>
        val after1Move = move(player.robot.direction, game)
        val after2Move = move(player.robot.direction, after1Move)
        after2Move
    }
  }


  def pushRobots(position: Position, direction: Direction, gameRunning: Game): Game =
    gameRunning.players.find(_.robot.position == position) match {
      case Some(player) =>
        val nextPos = direction(position)
        val rec = pushRobots(nextPos, direction, gameRunning)
        Events.move(player, nextPos)(rec)
      case None => gameRunning
    }


  private def movementIsAllowed(game: Game, position: Position, direction: Direction): Boolean = {
    val walls = game.scenario.walls
    val blockedByWall = direction match {
      case Up        => walls.contains(Wall(Up(position), Down))
      case UpRight   => walls.contains(Wall(position, UpRight))
      case DownRight => walls.contains(Wall(position, DownRight))
      case Down      => walls.contains(Wall(position, Down))
      case DownLeft  => walls.contains(Wall(DownLeft(position), UpRight))
      case UpLeft    => walls.contains(Wall(UpLeft(position), DownRight))
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
