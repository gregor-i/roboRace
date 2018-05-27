package repo

import gameLogic.{GameScenario, GameStarting, InitialGame}
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class GameRepositorySpec extends FunSuite with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach{
  def repo = app.injector.instanceOf[GameRepository]

  val g1 = ("initial", InitialGame)
  val g2 = ("starting", GameStarting(GameScenario.default, List.empty))

  override def beforeEach = {
    repo.list().foreach(g => repo.delete(g._1))
    repo.save(g1._1, g1._2)
    repo.save(g2._1, g2._2)
  }

  test("save") {
    // tested by beforeEach
  }

  test("get") {
    repo.get(g1._1) shouldBe Some(g1._2)
    repo.get(g2._1) shouldBe Some(g2._2)
  }

  test("list") {
    repo.list() shouldBe List(g1, g2)
  }

  test("delete") {
    repo.delete(g1._1)
    repo.get(g1._1) shouldBe None
    repo.list() shouldBe List(g2)
  }
}
