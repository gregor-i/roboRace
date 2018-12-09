package gameLogic
package command

import gameLogic.gameUpdate.DealOptions

sealed trait Command {
  def apply(player: String): Game => CommandResponse
}

case class RegisterForGame(index: Int) extends Command {
  def apply(player: String): Game => CommandResponse = {
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
        Game.players.modify(players => players :+ newPlayer)(game)
          .log(PlayerJoinedGame(newPlayer.index, newPlayer.robot))
      )
  }
}

case object DeregisterForGame extends Command {
  def apply(playerName: String): Game => CommandResponse = {
    case game if !game.players.exists(_.name == playerName)                           =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == playerName).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)

    case game if game.cycle == 0 =>
      val player = game.players.find(_.name == playerName).get
      CommandAccepted(
        Game.players.modify(_.filter(_ != player))(game)
          .log(PlayerQuitted(player.index, player.robot))
      )
    case game                    =>
      val player = game.players.find(_.name == playerName).get
      CommandAccepted(
        (Game.player(playerName) composeLens Player.finished)
          .set(Some(FinishedStatistic(game.players.count(_.finished.isEmpty), game.cycle, true)))(game)
          .log(PlayerQuitted(player.index, player.robot))
      )

  }
}

case class SetInstruction(cycle: Int, slot: Int, instruction: Int) extends Command {
  def apply(player: String): Game => CommandResponse = {
    case game if game.cycle != cycle =>
      CommandRejected(WrongCycle)
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)
    case game if 0 > slot || slot >= Constants.instructionsPerCycle =>
      CommandRejected(InvalidSlot)
    case game if 0 > instruction || instruction >= Constants.instructionOptionsPerCycle =>
      CommandRejected(InvalidActionChoice)
    case game if game.players.find(_.name == player).get.instructionSlots.contains(Some(instruction)) =>
      CommandRejected(ActionAlreadyUsed)
    case game =>
      CommandAccepted((Game.player(player) composeLens Player.instructionSlots).modify(_.updated(slot, Some(instruction)))(game))
  }
}

case class ResetInstruction(cycle: Int, slot: Int) extends Command {
  def apply(player: String): Game => CommandResponse = {
    case game if game.cycle != cycle =>
      CommandRejected(WrongCycle)
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)
    case game if 0 > slot || slot >= Constants.instructionsPerCycle =>
      CommandRejected(InvalidSlot)
    case game =>
      CommandAccepted((Game.player(player) composeLens Player.instructionSlots).modify(_.updated(slot, None))(game))
  }
}

