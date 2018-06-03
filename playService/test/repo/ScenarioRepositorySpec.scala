package repo

import gameLogic.{GameScenario, GameStarting, InitialGame}
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class ScenarioRepositorySpec extends FunSuite with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach {
  def repo = app.injector.instanceOf[ScenarioRepository]

  val s1 = ScenarioRow("s1", "player", GameScenario.default)
  val s2 = ScenarioRow("s2", "player", GameScenario.default)

  override def beforeEach = {
    repo.list().foreach(row => repo.delete(row.id))
    repo.save(s1)
    repo.save(s2)
  }

  test("save") {
    // tested by beforeEach
  }

  test("get") {
    repo.get(s1.id) shouldBe Some(s1)
    repo.get(s2.id) shouldBe Some(s2)
  }

  test("list") {
    repo.list() shouldBe List(s1, s2)
  }

  test("delete") {
    repo.delete(s1.id)
    repo.get(s1.id) shouldBe None
    repo.list() shouldBe List(s2)
  }
}
