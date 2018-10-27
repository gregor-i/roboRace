package roboRace.ai

import gameLogic._
import org.scalatest.tagobjects.Slow
import org.scalatest.{FunSuite, Matchers}

class LearnSpec extends FunSuite with BotHelper with Matchers {
  def filtering(scenario: Scenario, iterations: Int = 1): NeuronalNetworkGenes => Boolean = genes => botFinishesGame(NeuronalNetwork(genes), iterations, scenario)

  val scenario1 = Scenario(1, 3, Position(0, 0), Seq(Robot(Position(0, 2), Up)), Seq.empty, Seq.empty, Seq.empty)
  val scenario2 = Scenario(2,2, Position(0, 0), Seq(Robot(Position(1, 1), Up)), Seq.empty, Seq.empty, Seq.empty)
  val scenario3 = Scenario(2,1, Position(1, 0), Seq(Robot(Position(0, 0), Up)), Seq.empty, Seq.empty, Seq.empty)

  val filter1 = filtering(scenario1)
  val filter2 = filtering(scenario2)
  val filter3 = filtering(scenario3)

  val initialGenePool = Seq.tabulate(1000)(i => NeuronalNetwork.genesFromSeed(i.toLong))
  val breed: Seq[NeuronalNetworkGenes] => Seq[NeuronalNetworkGenes] = NeuronalNetwork.breed(_, 1000, 1L)

  implicit class EnrichPredicate[A](f: A => Boolean){
    def &&(g: A => Boolean): A => Boolean = a => f(a) && g(a)
  }

  ignore("validate solvability of scenarios"){
    sequenceWithAutoCycle(createGame(scenario1)(bot))(
      forcedInstructions(bot)(MoveForward, MoveForward),
      assertCycle(1),
      assert(_.players.head.finished.isDefined shouldBe true)
    )

    sequenceWithAutoCycle(createGame(scenario2)(bot))(
      forcedInstructions(bot)(TurnLeft, MoveForward, TurnRight, MoveForward),
      assertCycle(1),
      assert(_.players.head.finished.isDefined shouldBe true)
    )

    sequenceWithAutoCycle(createGame(scenario3)(bot))(
      forcedInstructions(bot)(TurnRight, TurnRight, MoveForward),
      assertCycle(1),
      assert(_.players.head.finished.isDefined shouldBe true)
    )
  }

  test("learning a scenario multiple times, increases the gene pool", Slow) {
    val gen0 = initialGenePool.filter(filter1)
    all(gen0.map(filter1)) shouldBe true
    gen0.size should be < 50
    gen0.size should be > 0

    val gen1 = Learn.learn(filter1, breed)(gen0)
    gen1.size should be > gen0.size
    all(gen1.map(filter1)) shouldBe true

    val gen2 = Learn.learn(filter1, breed)(gen1)
    gen2.size should be > gen1.size
    all(gen2.map(filter1)) shouldBe true

    val gen10 = NTimes(8)(Learn.learn(filter1, breed))(gen2)
    gen10.size should be > gen2.size
    gen10.size should be > 600
    all(gen10.map(filter1)) shouldBe true
  }

  test("learn scenario2", Slow) {
    val learnedGenePool = NTimes(10)(Learn.learn(filter2, breed))(initialGenePool)
    learnedGenePool.size should be > 600
  }

  test("learn scenario3", Slow) {
    val learnedGenePool = NTimes(10)(Learn.learn(filter3, breed))(initialGenePool)
    learnedGenePool.size should be > 600
  }

  ignore("learn multiple scenarios at the same time") {
    lazy val learnedGenePool1 = NTimes(10)(Learn.learn(filter1, breed))(initialGenePool)
    lazy val learnedGenePool2 = NTimes(10)(Learn.learn(filter2, breed))(initialGenePool)
    lazy val learnedGenePool3 = NTimes(10)(Learn.learn(filter3, breed))(initialGenePool)

    lazy val learnedGenePool12 = NTimes(10)(Learn.learn(filter1 && filter2, breed))(learnedGenePool1 ++ learnedGenePool2)
    lazy val learnedGenePool23 = NTimes(10)(Learn.learn(filter2 && filter3, breed))(learnedGenePool2 ++ learnedGenePool3)
    lazy val learnedGenePool13 = NTimes(10)(Learn.learn(filter1 && filter3, breed))(learnedGenePool1 ++ learnedGenePool3)

    lazy val learnedGenePool = NTimes(10)(Learn.learn(filter1 && filter2 && filter3, breed))(learnedGenePool12 ++ learnedGenePool13 ++ learnedGenePool23)
    println(s"learnedGenePool1.size = ${learnedGenePool1.size}")
    println(s"learnedGenePool2.size = ${learnedGenePool2.size}")
    println(s"learnedGenePool3.size = ${learnedGenePool3.size}")

    println(s"learnedGenePool12.size = ${learnedGenePool12.size}")
    println(s"learnedGenePool23.size = ${learnedGenePool23.size}")
    println(s"learnedGenePool13.size = ${learnedGenePool13.size}")

    println(s"learnedGenePool.size = ${learnedGenePool.size}")
    learnedGenePool.size should be > 50
  }

}
