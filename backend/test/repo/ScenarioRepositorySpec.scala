package repo

import api.{Entity, WithId}
import logic.DefaultScenario
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class ScenarioRepositorySpec extends AnyFunSuite with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach {
  def repo = app.injector.instanceOf[ScenarioRepository]

  val s1 = WithId(id = "s1", owner = "player", entity = Entity(description = "description1", value = DefaultScenario.default))
  val s2 = WithId(id = "s2", owner = "player", entity = Entity(description = "description2", value = DefaultScenario.default))

  override def beforeEach(): Unit = {
    repo.list().foreach(row => repo.delete(row.id))
    repo.save(id = s1.id, owner = s1.owner, entity = s1.entity)
    repo.save(id = s2.id, owner = s2.owner, entity = s2.entity)
  }

  test("save") {
    // tested by beforeEach
  }

  test("get") {
    repo.get(s1.id).collect(repo.rowToEntity) shouldBe Some(s1)
    repo.get(s2.id).collect(repo.rowToEntity) shouldBe Some(s2)
    repo.get("s3") shouldBe None
  }

  test("list") {
    repo.list().collect(repo.rowToEntity) shouldBe List(s1, s2)
  }

  test("delete") {
    repo.delete(s1.id)
    repo.get(s1.id) shouldBe None
    repo.list().collect(repo.rowToEntity) shouldBe List(s2)
  }
}
