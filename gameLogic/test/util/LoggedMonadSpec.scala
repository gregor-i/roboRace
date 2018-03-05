package util

import org.scalatest.{FunSuite, Matchers}

class LoggedMonadSpec extends FunSuite with Matchers {
  test("create Logged elements with apply") {
    val l = LoggedMonad(12, Seq("something"))
    l.state shouldBe 12
    l.events shouldBe Seq("something")
  }

  test("create Logged elements with pure") {
    val l = LoggedMonad.pure(13)
    l.state shouldBe 13
    l.events shouldBe Nil
  }

  test("concat Logged elements with flatMap") {
    val log = for {
      init <- LoggedMonad(1, Seq("initialized with 1"))
      added <- LoggedMonad(init + 3, Seq("added 3"))
      divided <- LoggedMonad((added / 2).toString, Seq("divided by 2", "to string"))
    } yield divided

    log.state shouldBe "2"
    log.events shouldBe Seq("initialized with 1", "added 3", "divided by 2", "to string")
  }

  test("map Logged elements") {
    val log = for {
      init <- LoggedMonad(1, Seq("initialized with 1"))
    } yield init.toString
    log.state shouldBe "1"
    log.events shouldBe Seq("initialized with 1")
  }

  test("provide a nicer syntax to create Logged elements") {
    import util.LoggedMonadSyntax._

    val log = for {
      init <- 1.log("initialized with 1")
      added <- (init + 3).log("added 3")
      divided <- (added / 2).toString.log("divided by 2", "to string")
    } yield divided

    log.state shouldBe "2"
    log.events shouldBe Seq("initialized with 1", "added 3", "divided by 2", "to string")
  }

  test("have the right variance") {
    import util.LoggedMonadSyntax._

    trait A
    class B extends A

    (new B).log(123) : LoggedMonad[A, Int]
  }
}
