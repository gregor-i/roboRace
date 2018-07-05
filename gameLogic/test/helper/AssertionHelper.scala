package helper

import gameLogic.Game
import org.scalatest.{Assertion, Matchers}

trait AssertionHelper {
  _: Matchers =>

  def assert(f: Game => Assertion): Game => Game =
    state => {
      f(state)
      state
    }
}
