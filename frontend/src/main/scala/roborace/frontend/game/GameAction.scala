package roborace.frontend.game

import roborace.frontend.GameFrontendState
import gameEntities.{Constants, Instruction, RunningPlayer, SetInstructions}

import scala.concurrent.Future

sealed trait GameAction {
  def apply(state: GameFrontendState): Future[GameFrontendState]
}

case class PlaceInstruction(instruction: Instruction, slot: Int) extends GameAction {
  def apply(state: GameFrontendState): Future[GameFrontendState] = {
    val newSlots = state.slots + (slot -> instruction)
    val newState = state.copy(slots = newSlots)
    if (newSlots.size == Constants.instructionsPerCycle) {
      SendCommand(newState, SetInstructions(newSlots.toSeq.sortBy(_._1).map(_._2)))
    } else {
      val slot = (for {
        i <- 0 until Constants.instructionsPerCycle
        s = (i + newState.focusedSlot) % Constants.instructionsPerCycle
        if newState.slots.get(s).isEmpty // intellij lies here!!!
      } yield s).headOption.getOrElse(0)
      Future.successful(newState.copy(focusedSlot = slot))
    }
  }
}

case class UnsetInstruction(slot: Int) extends GameAction {
  def apply(state: GameFrontendState): Future[GameFrontendState] = {
    val newSlots = state.slots - slot
    val newState = state.copy(slots = newSlots)
    state.game.you match {
      case Some(you: RunningPlayer) if you.instructionSlots.nonEmpty =>
        SendCommand(newState, gameEntities.ResetInstruction)
      case _ =>
        Future.successful(newState)
    }
  }
}
