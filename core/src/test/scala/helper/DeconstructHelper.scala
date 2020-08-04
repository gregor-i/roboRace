package helper

import gameEntities._
import gameLogic.command.Command
import gameLogic.gameUpdate._
import org.scalatest.matchers.should.Matchers

trait DeconstructHelper {
  _: Matchers =>

  implicit class EnrichCommand(val c: Command) {
    def apply(playerName: String): Game => CommandResponse = Command.apply(c, playerName)(_)
  }

  implicit class EnrichCommandReponseFunction(val f: Game => CommandResponse) {
    def accepted: Game => Game = state => {
      f(state) match {
        case CommandRejected(reason)   => fail(s"command was rejected with $reason")
        case CommandAccepted(newState) => Cycle(newState)
      }
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
