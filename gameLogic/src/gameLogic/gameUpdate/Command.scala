package gameLogic
package gameUpdate

sealed trait Command extends ((String, GameState) => CommandResponse) {
  def apply(player: String, gameState: GameState): CommandResponse = gameState match {
    case InitialGame => ifInitial(player)
    case g: GameStarting => ifStarting(player, g)
    case g: GameRunning => ifRunning(player, g)
    case g: GameFinished => ifFinished(player, g)
  }

  type IfInitial = String => CommandResponse
  type IfStarting = (String, GameStarting) => CommandResponse
  type IfRunning = (String, GameRunning) => CommandResponse
  type IfFinished = (String, GameFinished) => CommandResponse

  def ifInitial: IfInitial = _ => CommandRejected(WrongState)
  def ifStarting: IfStarting = (_, _) => CommandRejected(WrongState)
  def ifRunning: IfRunning = (_, _) => CommandRejected(WrongState)
  def ifFinished: IfFinished = (_, _) => CommandRejected(WrongState)
}

sealed trait CommandResponse
case class CommandRejected(reason: RejectionReason) extends CommandResponse
case class CommandAccepted(newState: GameState) extends CommandResponse

case class DefineScenario(scenario: GameScenario) extends Command {
  override def ifInitial: IfInitial =
    _ => CommandAccepted(GameStarting(scenario, Nil))
}

case object RegisterForGame extends Command {
  override def ifStarting: IfStarting = {
    case (player, g) if g.players.exists(_.name == player) =>
      CommandRejected(PlayerAlreadyRegistered)
    case (_, g) if g.players.size >= g.scenario.initialRobots.size =>
      CommandRejected(TooMuchPlayersRegistered)
    case (player, g) =>
      CommandAccepted(GameStarting.players.modify(players => players :+ StartingPlayer(players.size, player, ready = false))(g))
  }
}

case object ReadyForGame extends Command {
  override def ifStarting: IfStarting = {
    case (player, g) if !g.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case (player, g) =>
      CommandAccepted((GameStarting.player(player) composeLens StartingPlayer.ready).set(true)(g))
  }
}

case class ChooseInstructions(cycle: Int, instructions: Seq[Int]) extends Command {
  override def ifRunning: IfRunning = {
    case (_, g) if g.cycle != cycle =>
      CommandRejected(WrongCycle)
    case (player, g) if !g.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case (_, g) if instructions.size != Constants.instructionsPerCycle ||
      instructions.distinct.size != Constants.instructionsPerCycle ||
      instructions.forall(i => 0 > i || i > Constants.instructionOptionsPerCycle) =>
      CommandRejected(InvalidActionChoice)
    case (player, g) =>
      CommandAccepted(GameRunning.player(player).modify(p => p.copy(instructions = instructions.map(p.instructionOptions)))(g))
  }
}
