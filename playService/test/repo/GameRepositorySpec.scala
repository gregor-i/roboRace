package repo

import gameLogic.{Game, Scenario}
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class GameRepositorySpec extends FunSuite with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach{
  def repo = app.injector.instanceOf[GameRepository]

  val g1 = GameRow("initial", "player", Some(Game(2, Scenario.default, List.empty)))
  val g2 = GameRow("starting", "player", Some(Game(0, Scenario.default, List.empty)))

  override def beforeEach = {
    repo.list().foreach(row => repo.delete(row.id))
    repo.save(g1)
    repo.save(g2)
  }

  test("save") {
    // tested by beforeEach
  }

  test("get") {
    repo.get(g1.id) shouldBe Some(g1)
    repo.get(g2.id) shouldBe Some(g2)
  }

  test("list") {
    repo.list() shouldBe List(g1, g2)
  }

  test("delete") {
    repo.delete(g1.id)
    repo.get(g1.id) shouldBe None
    repo.list() shouldBe List(g2)
  }
}
