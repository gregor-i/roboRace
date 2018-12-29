package frontend.game

import gameEntities.{Constants, Instruction, SetInstructions}

import scala.concurrent.Future

sealed trait GameAction {
  def apply(state: GameState): Future[GameState]
}

case class PlaceInstruction(instruction: Instruction, slot: Int) extends GameAction {
  def apply(state: GameState): Future[GameState] = {
    val newSlots = state.slots + (slot -> instruction)
    val newState = state.copy(slots = newSlots)
    if (newSlots.size == Constants.instructionsPerCycle) {
      SendCommand(newState, SetInstructions(newSlots.toSeq.sortBy(_._1).map(_._2)))
    } else {
      val slot = (for {
        i <- 0 until Constants.instructionsPerCycle
        s = (i + newState.focusedSlot) % Constants.instructionsPerCycle
        if newState.slots.get(s).isEmpty
      } yield s).headOption.getOrElse(0)
      Future.successful(newState.copy(focusedSlot = slot))
    }
  }
}

case class UnsetInstruction(slot: Int) extends GameAction {
  def apply(state: GameState): Future[GameState] = {
    val newSlots = state.slots - slot
    val newState = state.copy(slots = newSlots)
    if (state.game.you.exists(_.instructionSlots.nonEmpty))
      SendCommand(newState, gameEntities.ResetInstruction)
    else
      Future.successful(newState)
  }
}
