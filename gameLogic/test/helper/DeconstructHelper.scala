package helper

import gameLogic.gameUpdate.{CommandAccepted, CommandRejected, CommandResponse, Cycle}
import gameLogic.{EventLog, GameState, Logged, RejectionReason}
import org.scalatest.{Assertion, Matchers}

trait DeconstructHelper {
  _: Matchers =>

  implicit class EnrichLogged(val f: GameState => Logged[GameState]) {
    def noEvents: GameState => GameState =
      state => {
        val r = f(state)
        r.events shouldBe Seq.empty
        r.state
      }

    def anyEvents: GameState => GameState =
      state => {
        val r = f(state)
        r.state
      }


    def logged(assets: Seq[EventLog] => Assertion): GameState => GameState =
      state => {
        val r = f(state)
        assets(r.events)
        r.state
      }
  }

  implicit class EnrichCommandReponse(val f: GameState => CommandResponse) {
    def accepted: GameState => Logged[GameState] = state => {
      val r = f(state)
      r shouldBe a[CommandAccepted]
      val newState = r.asInstanceOf[CommandAccepted].newState
      Cycle(newState)
    }

    def rejected(): GameState => GameState =
      state => {
        val r = f(state)
        r shouldBe a[CommandRejected]
        state
      }

    def rejected(expectedReason: RejectionReason): GameState => GameState =
      state => {
        val r = f(state)
        r shouldBe a[CommandRejected]
        r.asInstanceOf[CommandRejected].reason shouldBe expectedReason
        state
      }
  }

}
