package gameLogic
package gameUpdate

sealed trait Command {
  def apply(player: String): GameState => CommandResponse = {
    case InitialGame => ifInitial(player)
    case g: GameRunning => ifRunning(player)(g)
    case g: GameFinished => ifFinished(player)(g)
  }

  type IfInitial = String => CommandResponse
  type IfRunning = String => GameRunning => CommandResponse
  type IfFinished = String => GameFinished => CommandResponse

  def ifInitial: IfInitial = _ => CommandRejected(WrongState)
  def ifRunning: IfRunning = _ => _ => CommandRejected(WrongState)
  def ifFinished: IfFinished = _ => _ => CommandRejected(WrongState)
}

sealed trait CommandResponse
case class CommandRejected(reason: RejectionReason) extends CommandResponse
case class CommandAccepted(newState: GameState) extends CommandResponse

case class DefineScenario(scenario: GameScenario) extends Command {
  override def ifInitial: IfInitial = {
    case _ if !GameScenario.validation(scenario) =>
      CommandRejected(InvalidScenario)
    case player =>
      val newPlayer = RunningPlayer(index = 0,
        name = player,
        robot = scenario.initialRobots(0),
        instructions = Seq.empty,
        instructionOptions = DealOptions.initial,
        finished = None
      )
      CommandAccepted(GameRunning(0, scenario, List(newPlayer)))
  }
}

case object RegisterForGame extends Command {
  override def ifRunning: IfRunning = player => {
    case game if game.cycle != 0 =>
      CommandRejected(WrongCycle)
    case game if game.players.exists(_.name == player) =>
      CommandRejected(PlayerAlreadyRegistered)
    case game if game.players.size >= game.scenario.initialRobots.size =>
      CommandRejected(TooMuchPlayersRegistered)
    case game =>
      val newPlayer = RunningPlayer(index = game.players.size,
        name = player,
        robot = game.scenario.initialRobots(game.players.size),
        instructions = Seq.empty,
        instructionOptions = DealOptions.initial,
        finished = None
      )
      CommandAccepted(GameRunning.players.modify(players => players :+ newPlayer)(game))
  }
}

case object DeregisterForGame extends Command {
  override def ifRunning: IfRunning = player => {
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)

    case game if game.cycle == 0 =>
      CommandAccepted(GameRunning.players.modify(_.filter(_.name != player).zipWithIndex.map{case (player, index) => player.copy(index =index)})(game))
    case game =>
      CommandAccepted((GameRunning.player(player) composeLens RunningPlayer.finished)
        .set(Some(FinishedStatistic(game.players.count(_.finished.isEmpty), game.cycle, true)))(game))
  }
}

case class ChooseInstructions(cycle: Int, instructions: Seq[Int]) extends Command {
  override def ifRunning: IfRunning = player => {
    case game if game.cycle != cycle =>
      CommandRejected(WrongCycle)
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)
    case game if instructions.size != Constants.instructionsPerCycle ||
      instructions.distinct.size != Constants.instructionsPerCycle ||
      instructions.forall(i => 0 > i && i > Constants.instructionOptionsPerCycle) =>
      CommandRejected(InvalidActionChoice)
    case game =>
      CommandAccepted(GameRunning.player(player).modify(p => p.copy(instructions = instructions.map(p.instructionOptions)))(game))
  }
}

