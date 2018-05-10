package gameLogic
package gameUpdate

object MoveRobots {
  def apply(player: RunningPlayer, action: MoveAction, game: GameRunning): Logged[GameRunning] = {
    def move(direction: Direction, gameRunning: GameRunning): Logged[GameRunning] ={
      val p = gameRunning.players.find(_.name == player.name).get
      if (movementIsAllowed(gameRunning, p.robot.position, direction)) {
        for{
          afterPush <- MoveRobots.pushRobots(p.robot.position, direction, gameRunning)
          afterEffects <- ScenarioEffects.afterMoveAction(afterPush)
        } yield afterEffects
      }else{
        gameRunning.log(RobotMovementBlocked(p.name, p.robot.position, direction))
      }
    }

    action match {
      case MoveForward => move(player.robot.direction, game)
      case MoveBackward => move(player.robot.direction.back, game)
      case MoveTwiceForward =>
        for{
          after1Move <- move(player.robot.direction, game)
          after2Move <- move(player.robot.direction, after1Move)
        } yield after2Move
    }
  }


  def pushRobots(position: Position, direction: Direction, gameRunning: GameRunning): Logged[GameRunning] =
    gameRunning.players.find(_.robot.position == position) match {
      case Some(player) =>
        val nextPos = player.robot.position.move(direction)
        for {
          pushedRobots <- pushRobots(position.move(direction), direction, gameRunning)
          nextRobotState <- player.robot.copy(position = nextPos).log(RobotPositionTransition(player.name, player.robot.position, nextPos))
        } yield pushedRobots.copy(players = pushedRobots.players.map(p => if (p.name == player.name) p.copy(robot = nextRobotState) else p))
      case None => Logged.pure(gameRunning)
    }


  private def movementIsAllowed(game: GameRunning, position: Position, direction: Direction): Boolean = {
    val downWalls = game.scenario.walls.filter(_.direction == Down).map(_.position)
    val rightWalls = game.scenario.walls.filter(_.direction == Right).map(_.position)
    //    if (game.scenario.beaconPosition == position.move(direction))
    //      false
    if (direction == Down && downWalls.contains(position))
      false
    else if (direction == Right && rightWalls.contains(position))
      false
    else if (direction == Up && downWalls.contains(position.move(Up)))
      false
    else if (direction == Left && rightWalls.contains(position.move(Left)))
      false
    else if (game.players.exists(_.robot.position == position.move(direction)))
      movementIsAllowed(game, position.move(direction), direction)
    else
      true
  }
}
