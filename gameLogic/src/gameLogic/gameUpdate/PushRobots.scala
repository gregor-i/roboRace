package gameLogic
package gameUpdate

object PushRobots {

  def apply(position: Position, direction: Direction, gameRunning: GameRunning): Logged[GameRunning] =
    gameRunning.players.find(_.robot.position == position) match {
      case Some(player) =>
        val nextPos = player.robot.position.move(direction)
        for {
          pushedRobots <- PushRobots(position.move(direction), direction, gameRunning)
          nextRobotState <- player.robot.copy(position = nextPos).log(RobotPositionTransition(player.name, player.robot.position, nextPos))
        } yield pushedRobots.copy(players = gameRunning.players.map(p => if (p.name == player.name) p.copy(robot = nextRobotState) else p))
      case None => Logged.pure(gameRunning)
    }
}
