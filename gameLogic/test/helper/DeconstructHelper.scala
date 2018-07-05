package helper

import gameLogic.gameUpdate._
import gameLogic.{EventLog, Game, Logged, RejectionReason}
import org.scalatest.{Assertion, Matchers}

trait DeconstructHelper {
  _: Matchers =>

  implicit class EnrichLogged(val f: Game => Logged[Game]) {
    def noEvents: Game => Game =
      state => {
        val r = f(state)
        r.events shouldBe Seq.empty
        r.state
      }

    def anyEvents: Game => Game =
      state => {
        val r = f(state)
        r.state
      }


    def logged(assets: Seq[EventLog] => Assertion): Game => Game =
      state => {
        val r = f(state)
        assets(r.events)
        r.state
      }
  }


  implicit class EnrichCommandReponseFunction(val f: Game => CommandResponse) {
    def accepted: Game => Logged[Game] = state => {
      val r = f(state)
      r shouldBe a[CommandAccepted]
      val newState = r.asInstanceOf[CommandAccepted].newState
      Cycle(newState)
    }

    def rejected(): Game => Game =
      state => {
        val r = f(state)
        r shouldBe a[CommandRejected]
        state
      }

    def rejected(expectedReason: RejectionReason): Game => Game =
      state => {
        val r = f(state)
        r shouldBe a[CommandRejected]
        r.asInstanceOf[CommandRejected].reason shouldBe expectedReason
        state
      }
  }

}
