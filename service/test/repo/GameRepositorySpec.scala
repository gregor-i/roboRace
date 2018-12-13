package repo

import java.time.ZonedDateTime

import gameLogic.DefaultScenario
import gameEntities._
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class GameRepositorySpec extends FunSuite with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach{
  def repo = app.injector.instanceOf[GameRepository]

  val g1 = GameRow(
    id = "initial",
    owner = "player",
    game = Some(Game(2, DefaultScenario.default, List.empty, Seq(RobotTurns(0, Position(0,0), UpRight, Up)))),
    creationTime = ZonedDateTime.now().withNano(0))
  val g2 = GameRow(
    id = "starting",
    owner = "player",
    game = Some(Game(0, DefaultScenario.default, List.empty, Seq())),
    creationTime = ZonedDateTime.now().withNano(0))

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
