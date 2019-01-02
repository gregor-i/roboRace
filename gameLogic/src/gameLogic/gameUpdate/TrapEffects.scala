package gameLogic
package gameUpdate

import gameEntities._

object TrapEffects {

  def applyAll(game: Game): Game =
    State.all(
      for {
        trap <- game.scenario.traps
        player <- Lenses.runningPlayers.getAll(game)
        if trap.position == player.robot.position
      } yield apply(trap, player)
    )(game)

  def afterMove(pushed: RobotPushed)(game: Game): Game = {
    val after1 = game.scenario.traps.find(_.position == pushed.to) match {
      case Some(trap) => apply(trap, Lenses.runningPlayer(pushed.player.id).getAll(game).head).apply(game)
      case None => game
    }
    pushed.push match {
      case Some(push) => afterMove(push)(after1)
      case None => after1
    }
  }

  def apply(trap: Trap, player: RunningPlayer): Game => Game =
    State.sequence(
      trap match {
        case _: TurnRightTrap =>
          Events.turn(player, Direction.turnRight(player.robot.direction))
        case _: TurnLeftTrap =>
          Events.turn(player, Direction.turnLeft(player.robot.direction))
        case _: StunTrap =>
          Events.stun(player)
      },
      Lenses.log(TrapEffect(player.index, trap))
    )
}
