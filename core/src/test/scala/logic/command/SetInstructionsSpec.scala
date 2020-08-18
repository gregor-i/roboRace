package logic.command

import entities._
import helper.GameUpdateHelper
import logic._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SetInstructionsSpec extends AnyFunSuite with Matchers with GameUpdateHelper {
  val initialGame = sequenceWithAutoCycle(createGame(DefaultScenario.default)(p0))(
    RegisterForGame(1)(p1).accepted,
    clearHistory
  )

  test("reject command if the given player is not part of the game") {
    sequenceWithAutoCycle(initialGame)(
      SetInstructions(validInstructionSequence)(p2).rejected(PlayerNotFound)
    )
  }

  test("reject command if the given player already finished the game") {
    sequenceWithAutoCycle(initialGame)(
      Lenses.player(p0).modify(p => FinishedPlayer(p.index, p.id, 0, 0)),
      SetInstructions(validInstructionSequence)(p0).rejected(PlayerAlreadyFinished)
    )
  }

  test("reject command if the given instruction is outside of the accepted interval") {
    sequenceWithAutoCycle(initialGame)(
      SetInstructions(validInstructionSequence.drop(1))(p0).rejected(InvalidActionChoice),
      SetInstructions(validInstructionSequence :+ MoveBackward)(p0).rejected(InvalidActionChoice)
    )
  }

  test("accept the command otherwise") {
    sequenceWithAutoCycle(initialGame)(
      SetInstructions(validInstructionSequence)(p0).accepted,
      SetInstructions(validInstructionSequence)(p1).accepted,
      assertCycle(1)
    )
  }
}
