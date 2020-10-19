package repo

import java.time.ZonedDateTime

import api.WithId
import entities._
import logic.DefaultScenario
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class GameRepositorySpec extends AnyFunSuite with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach {
  def repo = app.injector.instanceOf[GameRepository]

  val g1 = WithId(
    id = "initial",
    owner = "player",
    entity = Game(2, DefaultScenario.default, List.empty, Seq(RobotTurns(0, Position(0, 0), UpRight, Up)))
  )
  val g2 = WithId(
    id = "starting",
    owner = "player",
    entity = Game(0, DefaultScenario.default, List.empty, Seq())
  )

  override def beforeEach(): Unit = {
    repo.list().foreach(row => repo.delete(row.id))
    repo.save(id = g1.id, owner = g1.owner, entity = g1.entity)
    repo.save(id = g2.id, owner = g2.owner, entity = g2.entity)
  }

  test("save") {
    // tested by beforeEach
  }

  test("get") {
    repo.get(g1.id).collect(repo.rowToEntity) shouldBe Some(g1)
    repo.get(g2.id).collect(repo.rowToEntity) shouldBe Some(g2)
  }

  test("list") {
    repo.list().collect(repo.rowToEntity) shouldBe List(g1, g2)
  }

  test("delete") {
    repo.delete(g1.id)
    repo.get(g1.id) shouldBe None
    repo.list().collect(repo.rowToEntity) shouldBe List(g2)
  }
}
