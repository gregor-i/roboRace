package gameLogic
package gameUpdate

object MoveRobots {
  def apply(player: RunningPlayer, instruction: MoveInstruction, game: Game): Logged[Game] = {
    def move(direction: Direction, gameRunning: Game): Logged[Game] ={
      val p = gameRunning.players.find(_.name == player.name).get
      if (movementIsAllowed(gameRunning, p.robot.position, direction)) {
        for{
          afterPush <- MoveRobots.pushRobots(p.robot.position, direction, gameRunning)
          afterEffects <- ScenarioEffects.afterMoveAction(afterPush)
        } yield afterEffects
      }else{
        gameRunning.log(MovementBlocked(p.name, p.robot.position, direction))
      }
    }

    instruction match {
      case MoveForward => move(player.robot.direction, game)
      case MoveBackward => move(player.robot.direction.back, game)
      case StepRight => move(player.robot.direction.right, game)
      case StepLeft => move(player.robot.direction.left, game)
      case MoveTwiceForward =>
        for{
          after1Move <- move(player.robot.direction, game)
          after2Move <- move(player.robot.direction, after1Move)
        } yield after2Move
    }
  }


  def pushRobots(position: Position, direction: Direction, gameRunning: Game): Logged[Game] =
    gameRunning.players.find(_.robot.position == position) match {
      case Some(player) =>
        val nextPos = direction(position)
        for {
          rec <- pushRobots(nextPos, direction, gameRunning)
          nextState <- Events.move(player, nextPos)(rec)
        } yield nextState
      case None => Logged.pure(gameRunning)
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
    else if (game.players.exists(_.robot.position == direction(position)))
      movementIsAllowed(game, direction(position), direction)
    else
      true
  }
}
