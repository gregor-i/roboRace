package gameLogic
package gameUpdate

import org.scalatest.{FunSuite, Matchers}

class CommandSpec extends FunSuite with Matchers with TestData {
  val starting = startingStateHelper()
  val withPlayer1 = startingStateHelper(false)
  val withPlayer1And2 = startingStateHelper(false, false)
  val withPlayer1Ready = startingStateHelper(true, false)
  val withPlayer1And2Ready = startingStateHelper(true, true)

  test("RegisterForGame: add players") {
    val after1 = RegisterForGame(p1, starting).newState
    after1 shouldBe withPlayer1
    val after2 = RegisterForGame(p2, after1).newState
    after2 shouldBe withPlayer1And2
  }

  test("RegisterForGame: reject players with the same name") {
    RegisterForGame(p1, withPlayer1).rejectedReason shouldBe PlayerAlreadyRegistered
  }

  test("RegisterForGame: reject players if there are to many") {
    val filledGame = starting.copy(scenario = s.copy(initialRobots = List.empty))
    RegisterForGame(p1, filledGame).rejectedReason shouldBe TooMuchPlayersRegistered
  }

  test("ReadyForGame: start game if all players are ready") {
    val after1 = ReadyForGame(p1, withPlayer1And2).newState
    after1 shouldBe withPlayer1Ready
    val after2 = ReadyForGame(p2, withPlayer1Ready).newState
    after2 shouldBe withPlayer1And2Ready
  }

  implicit class CommandResponseHelper(val r: CommandResponse) {
    def newState: GameState = {
      r shouldBe a [CommandAccepted]
      r.asInstanceOf[CommandAccepted].newState
    }

    def rejectedReason: RejectionReason = {
      r shouldBe a [CommandRejected]
      r.asInstanceOf[CommandRejected].reason
    }
  }
}
