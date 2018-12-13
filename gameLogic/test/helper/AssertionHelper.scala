package helper

import gameEntities.{EventLog, Game, Player}
import org.scalatest.{Assertion, Matchers}

trait AssertionHelper {
  _: Matchers =>

  def assert(f: Game => Assertion): CE =
    game => {
      f(game)
      game
    }

  def assertCycle(n: Int): CE = assert(_.cycle shouldBe n)

  def assertPlayer(name: String)(f: Player => Assertion): CE =
    assert(game => f(game.players.find(_.name == name).get))

  def assertLog(f: Seq[EventLog] => Assertion): CE =
    assert(game => f(game.events))
}
