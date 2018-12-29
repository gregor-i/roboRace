package gameLogic.command

import gameEntities._
import gameLogic._
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class ResetInstructionSpec extends FunSuite with Matchers with GameUpdateHelper {
  val initialGame = sequenceWithAutoCycle(createGame()(p0))(
    RegisterForGame(1)(p1).accepted,
    clearHistory
  )

  test("reject command if the given player is not part of the game") {
    sequenceWithAutoCycle(initialGame)(
      ResetInstruction(p2).rejected(PlayerNotFound)
    )
  }

  test("reject command if the given player already finished the game") {
    sequenceWithAutoCycle(initialGame)(
      Lenses.finished(p0).set(Some(FinishedStatistic(0, 0, false))),
      ResetInstruction(p0).rejected(PlayerAlreadyFinished)
    )
  }

  test("accept the command otherwise") {
    sequenceWithAutoCycle(initialGame)(
      SetInstructions(validInstructionSequence)(p0).accepted,
      ResetInstruction(p0).accepted,

      SetInstructions(validInstructionSequence)(p1).accepted,
      assertCycle(0),

      SetInstructions(validInstructionSequence)(p0).accepted,
      assertCycle(1)
    )
  }
}
