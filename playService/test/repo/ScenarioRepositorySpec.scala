package repo

import gameLogic.{GameScenario, GameStarting, InitialGame}
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class ScenarioRepositorySpec extends FunSuite with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach{
  def repo = app.injector.instanceOf[ScenarioRepository]

  val s1 = ("s1", GameScenario.default)
  val s2 = ("s2", GameScenario.default)

  override def beforeEach = {
    repo.list().foreach(g => repo.delete(g._1))
    repo.save(s1._1, s1._2)
    repo.save(s2._1, s2._2)
  }

  test("save") {
    // tested by beforeEach
  }

  test("get") {
    repo.get(s1._1) shouldBe Some(s1._2)
    repo.get(s2._1) shouldBe Some(s2._2)
  }

  test("list") {
    repo.list() shouldBe List(s1, s2)
  }

  test("delete") {
    repo.delete(s1._1)
    repo.get(s1._1) shouldBe None
    repo.list() shouldBe List(s2)
  }
}
