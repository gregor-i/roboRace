package gameLogic
package command

import gameEntities._
import gameLogic.gameUpdate.DealOptions

object Command {
  def apply(command: Command, player: String)(game: Game): CommandResponse = command match {
    case RegisterForGame(index) => registerForGame(index)(player)(game)
    case DeregisterForGame => deregisterForGame(player)(game)
    case SetInstruction(cycle, slot, instruction) => setInstruction(cycle, slot, instruction)(player)(game)
    case ResetInstruction(cycle, slot) => resetInstruction(cycle, slot)(player)(game)
  }

  def registerForGame(index: Int)(player: String): Game => CommandResponse = {
    case game if game.cycle != 0 =>
      CommandRejected(WrongCycle)
    case game if game.players.exists(_.name == player) =>
      CommandRejected(PlayerAlreadyRegistered)
    case game if !game.scenario.initialRobots.indices.contains(index) =>
      CommandRejected(InvalidIndex)
    case game if game.players.exists(_.index == index) =>
      CommandRejected(RobotAlreadyTaken)
    case game =>
      val newPlayer = Player(
        index = index,
        name = player,
        robot = game.scenario.initialRobots(index),
        instructionSlots = Instruction.emptySlots,
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
    case game if !game.players.exists(_.name == playerName) =>
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
    case game =>
      val player = game.players.find(_.name == playerName).get
      CommandAccepted(
        State.sequence(
          Lenses.finished(playerName)
            .set(Some(FinishedStatistic(game.players.count(_.finished.isEmpty), game.cycle, true))),
          Lenses.log(PlayerQuitted(player.index, player.robot))
        )(game)
      )

  }

  def setInstruction(cycle: Int, slot: Int, instruction: Int)(player: String): Game => CommandResponse = {
    case game if game.cycle != cycle =>
      CommandRejected(WrongCycle)
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)
    case game if 0 > slot || slot >= Constants.instructionsPerCycle =>
      CommandRejected(InvalidSlot)
    case game if !game.players.find(_.name == player).get.instructionOptions.indices.contains(instruction) =>
      CommandRejected(InvalidActionChoice)
    case game if game.players.find(_.name == player).get.instructionSlots.contains(Some(instruction)) =>
      CommandRejected(ActionAlreadyUsed)
    case game =>
      CommandAccepted(Lenses.instructionSlots(player).modify(_.updated(slot, Some(instruction)))(game))
  }

  def resetInstruction(cycle: Int, slot: Int)(player: String): Game => CommandResponse = {
    case game if game.cycle != cycle =>
      CommandRejected(WrongCycle)
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)
    case game if 0 > slot || slot >= Constants.instructionsPerCycle =>
      CommandRejected(InvalidSlot)
    case game =>
      CommandAccepted(Lenses.instructionSlots(player).modify(_.updated(slot, None))(game))
  }
}

