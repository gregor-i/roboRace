package helper

import gameLogic.gameUpdate._
import gameLogic.{Game, RejectionReason}
import org.scalatest.Matchers

trait DeconstructHelper {
  _: Matchers =>


  implicit class EnrichCommandReponseFunction(val f: Game => CommandResponse) {
    def accepted: Game => Game = state => {
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
