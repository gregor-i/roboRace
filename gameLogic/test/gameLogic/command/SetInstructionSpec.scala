package gameLogic.command

import gameEntities._
import gameLogic._
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class SetInstructionSpec extends FunSuite with Matchers with GameUpdateHelper {
  val initialGame = sequenceWithAutoCycle(createGame(DefaultScenario.default)(p0))(
    RegisterForGame(1)(p1).accepted,
    clearHistory
  )

  test("reject command if the given cycle is wrong") {
    sequenceWithAutoCycle(initialGame)(
      assertCycle(0),
      SetInstruction(cycle = 1, slot = 0, instruction = 0)(p0).rejected(WrongCycle)
    )
  }

  test("reject command if the given player is not part of the game") {
    sequenceWithAutoCycle(initialGame)(
      SetInstruction(cycle = 0, slot = 0, instruction = 0)(p2).rejected(PlayerNotFound)
    )
  }

  test("reject command if the given player already finished the game") {
    sequenceWithAutoCycle(initialGame)(
      Lenses.finished(p0).set(Some(FinishedStatistic(0, 0, false))),
      SetInstruction(cycle = 0, slot = 0, instruction = 0)(p0).rejected(PlayerAlreadyFinished)
    )
  }

  test("reject command if the given slot is outside of the accepted interval") {
    sequenceWithAutoCycle(initialGame)(
      SetInstruction(cycle = 0, slot = -1, instruction = 0)(p0).rejected(InvalidSlot),
      SetInstruction(cycle = 0, slot = 6, instruction = 0)(p0).rejected(InvalidSlot)
    )
  }

  test("reject command if the given instruction is outside of the accepted interval") {
    sequenceWithAutoCycle(initialGame)(
      SetInstruction(cycle = 0, slot = 0, instruction = -1)(p0).rejected(InvalidActionChoice),
      SetInstruction(cycle = 0, slot = 0, instruction = initialGame.players.head.instructionOptions.size)(p0).rejected(InvalidActionChoice)
    )
  }

  test("reject command if the given instruction has already been used") {
    sequenceWithAutoCycle(initialGame)(
      SetInstruction(cycle = 0, slot = 0, instruction = 0)(p0).accepted,
      SetInstruction(cycle = 0, slot = 1, instruction = 0)(p0).rejected(ActionAlreadyUsed)
    )
  }

  test("accept the command otherwise") {
    sequenceWithAutoCycle(initialGame)(
      SetInstruction(cycle = 0, slot = 0, instruction = 0)(p0).accepted,
      SetInstruction(cycle = 0, slot = 1, instruction = 1)(p0).accepted,
      SetInstruction(cycle = 0, slot = 2, instruction = 2)(p0).accepted,
      SetInstruction(cycle = 0, slot = 3, instruction = 3)(p0).accepted,
      SetInstruction(cycle = 0, slot = 4, instruction = 4)(p0).accepted,

      SetInstruction(cycle = 0, slot = 0, instruction = 0)(p1).accepted,
      SetInstruction(cycle = 0, slot = 1, instruction = 1)(p1).accepted,
      SetInstruction(cycle = 0, slot = 2, instruction = 2)(p1).accepted,
      SetInstruction(cycle = 0, slot = 3, instruction = 3)(p1).accepted,
      SetInstruction(cycle = 0, slot = 4, instruction = 4)(p1).accepted,

      assertCycle(1)
    )
  }
}
