package helper

import entities._
import logic.command.{Command, CommandResponse}
import logic.gameUpdate._
import org.scalatest.matchers.should.Matchers

trait DeconstructHelper {
  _: Matchers =>

  implicit class EnrichCommand(val c: Command) {
    def apply(playerName: String): Game => CommandResponse = Command.apply(c, playerName)(_)
  }

  implicit class EnrichCommandResponseFunction(val f: Game => CommandResponse) {
    def accepted: Game => Game = state => {
      f(state) match {
        case Left(reason)    => fail(s"command was rejected with $reason")
        case Right(newState) => Cycle(newState)
      }
    }

    def rejected(): Game => Game =
      state => {
        val r = f(state)
        assert(r.isLeft)
        state
      }

    def rejected(expectedReason: RejectionReason): Game => Game =
      state => {
        val r = f(state)
        r match {
          case Right(_)     => fail("command was not rejected")
          case Left(reason) => assert(reason == expectedReason)
        }
        state
      }
  }

}
