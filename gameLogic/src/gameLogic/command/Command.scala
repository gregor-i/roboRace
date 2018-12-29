package gameLogic
package command

import gameEntities._
import gameLogic.gameUpdate.DealOptions

object Command {
  def apply(command: Command, player: String)(game: Game): CommandResponse = command match {
    case RegisterForGame(index)        => registerForGame(index)(player)(game)
    case DeregisterForGame             => deregisterForGame(player)(game)
    case SetInstructions(instructions) => setInstructions(instructions)(player)(game)
    case ResetInstruction              => resetInstruction(player)(game)
  }

  def registerForGame(index: Int)(player: String): Game => CommandResponse = {
    case game if game.cycle != 0                                      =>
      CommandRejected(WrongCycle)
    case game if game.players.exists(_.name == player)                =>
      CommandRejected(PlayerAlreadyRegistered)
    case game if !game.scenario.initialRobots.indices.contains(index) =>
      CommandRejected(InvalidIndex)
    case game if game.players.exists(_.index == index)                =>
      CommandRejected(RobotAlreadyTaken)
    case game                                                         =>
      val newPlayer = Player(
        index = index,
        name = player,
        robot = game.scenario.initialRobots(index),
        instructionSlots = Seq.empty,
        instructionOptions = DealOptions.initial,
        finished = None
      )
      CommandAccepted(
        State.sequence(
          Lenses.players.modify(players => players :+ newPlayer),
          Lenses.log(PlayerJoinedGame(newPlayer.index, newPlayer.robot))
        )(game)
      )
  }

  def deregisterForGame(playerName: String): Game => CommandResponse = {
    case game if !game.players.exists(_.name == playerName)                           =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == playerName).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)

    case game if game.cycle == 0 =>
      val player = game.players.find(_.name == playerName).get
      CommandAccepted(
        State.sequence(
          Lenses.players.modify(_.filter(_ != player)),
          Lenses.log(PlayerQuitted(player.index, player.robot))
        )(game)
      )
    case game                    =>
      val player = game.players.find(_.name == playerName).get
      CommandAccepted(
        State.sequence(
          Lenses.finished(playerName)
            .set(Some(FinishedStatistic(game.players.count(_.finished.isEmpty), game.cycle, true))),
          Lenses.log(PlayerQuitted(player.index, player.robot))
        )(game)
      )

  }

  def setInstructions(instructions: Seq[Instruction])(playerName: String)(game: Game): CommandResponse =
    game.players.find(_.name == playerName) match {
      case None                                      =>
        CommandRejected(PlayerNotFound)
      case Some(player) if player.finished.isDefined =>
        CommandRejected(PlayerAlreadyFinished)
      case _ if instructions.size != Constants.instructionsPerCycle =>
        CommandRejected(InvalidActionChoice)
      case Some(player) if !Instruction.instructions.forall(instr =>
        instructions.count(_ == instr) <= player.instructionOptions.find(_.instruction ==  instr).fold(0)(_.count)
      )                                              =>
        CommandRejected(InvalidActionChoice)
      case _                                         =>
        CommandAccepted(Lenses.instructionSlots(playerName).set(instructions)(game))
    }

  def resetInstruction(playerName: String)(game: Game): CommandResponse =
    game.players.find(_.name == playerName) match {
      case None                                      =>
        CommandRejected(PlayerNotFound)
      case Some(player) if player.finished.isDefined =>
        CommandRejected(PlayerAlreadyFinished)
      case _                                         =>
        CommandAccepted(Lenses.instructionSlots(playerName).set(Seq.empty)(game))
    }
}

