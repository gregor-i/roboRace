package gameLogic
package  gameUpdate

object TrapEffects {

  def applyAll(game: Game): Game =
    State.all(
      for {
        trap <- game.scenario.traps
        player <- game.players.find(_.robot.position == trap.position)
      } yield apply(trap, player)
    )(game)

  def afterMove(pushed: RobotPushed)(game: Game): Game = {
    val after1 = game.scenario.traps.find(_.position == pushed.to) match {
      case Some(trap) => apply(trap, game.players.find(_.index == pushed.player.index).get)(game)
      case None       => game
    }
    pushed.push match {
      case Some(push) => afterMove(push)(after1)
      case None => after1
    }
  }

  def apply(trap: Trap, player: Player): Game => Game =
    (trap match {
      case _: TurnRightTrap =>
        Events.turn(player, player.robot.direction.right)
      case _: TurnLeftTrap  =>
        Events.turn(player, player.robot.direction.left)
      case _: StunTrap      =>
        Events.stun(player)
    }).compose(_.log(TrapEffect(player.index, trap)))
}
