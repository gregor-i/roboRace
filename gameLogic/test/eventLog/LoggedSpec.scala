package eventLog

import gameLogic.eventLog.Logged
import org.scalatest.{FunSuite, Matchers}

class LoggedSpec extends FunSuite with Matchers {
  test("create Logged elements with apply") {
    val l = Logged(12, Seq("something"))
    l.state shouldBe 12
    l.events shouldBe Seq("something")
  }

  test("create Logged elements with pure") {
    val l = Logged.pure(13)
    l.state shouldBe 13
    l.events shouldBe Nil
  }

  test("concat Logged elements with flatMap") {
    val log = for {
      init <- Logged(1, Seq("initialized with 1"))
      added <- Logged(init + 3, Seq("added 3"))
      divided <- Logged((added / 2).toString, Seq("divided by 2", "to string"))
    } yield divided

    log.state shouldBe "2"
    log.events shouldBe Seq("initialized with 1", "added 3", "divided by 2", "to string")
  }

  test("map Logged elements") {
    val log = for {
      init <- Logged(1, Seq("initialized with 1"))
    } yield init.toString
    log.state shouldBe "1"
    log.events shouldBe Seq("initialized with 1")
  }

  test("provide a nicer syntax to create Logged elements") {
    import gameLogic.eventLog.LoggedSyntax._

    val log = for {
      init <- 1.log("initialized with 1")
      added <- (init + 3).log("added 3")
      divided <- (added / 2).toString.log("divided by 2", "to string")
    } yield divided

    log.state shouldBe "2"
    log.events shouldBe Seq("initialized with 1", "added 3", "divided by 2", "to string")
  }

  test("have the right variance") {
    import gameLogic.eventLog.LoggedSyntax._

    trait A
    class B extends A

    (new B).log(123) : Logged[A, Int]
  }
}
