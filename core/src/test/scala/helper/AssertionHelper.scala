package helper

import entities._
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

trait AssertionHelper {
  _: Matchers =>

  def assert(f: Game => Assertion): CE =
    game => {
      f(game)
      game
    }

  def assertCycle(n: Int): CE = assert(_.cycle shouldBe n)

  def assertPlayer(name: String)(f: Player => Assertion): CE =
    assert(game => f(game.players.find(_.id == name).get))

  def assertRunningPlayer(id: String)(f: RunningPlayer => Assertion): CE =
    assert(game => f(game.players.find(_.id == id).get.asInstanceOf[RunningPlayer]))

  def assertFinishedPlayer(id: String)(f: FinishedPlayer => Assertion): CE =
    assert(game => f(game.players.find(_.id == id).get.asInstanceOf[FinishedPlayer]))

  def assertQuittedPlayer(id: String)(f: QuitedPlayer => Assertion): CE =
    assert(game => f(game.players.find(_.id == id).get.asInstanceOf[QuitedPlayer]))

  def assertLog(f: Seq[EventLog] => Assertion): CE =
    assert(game => f(game.events))
}
