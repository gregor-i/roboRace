package roboRace.ai

import gameLogic.{Position, Robot, Scenario, Up}

object Console extends App {
  val repo = new NeuronalNetworkRepository("neuronal-network-genes.json")

  def filtering(scenario: Scenario, iterations: Int = 1): NeuronalNetworkGenes => Boolean = genes => BotFinishesGame(iterations, scenario)(NeuronalNetwork(genes))

  val scenario1 = Scenario(1, 3, Position(0, 0), Seq(Robot(Position(0, 2), Up)), Seq.empty, Seq.empty, Seq.empty)
  val scenario2 = Scenario(2,2, Position(0, 0), Seq(Robot(Position(1, 1), Up)), Seq.empty, Seq.empty, Seq.empty)
  val scenario3 = Scenario(2,1, Position(1, 0), Seq(Robot(Position(0, 0), Up)), Seq.empty, Seq.empty, Seq.empty)

  val filter1 = filtering(scenario1)
  val filter2 = filtering(scenario2)
  val filter3 = filtering(scenario3)

  var iteration = 0
  while(true){
    println(s"Iteration $iteration:")
    val state = repo.read()
    println(s"current gene pool size: ${state.map(_.size)}")

    val nextState = state match {
      case None =>
        println("initializing gene pool")
        NeuronalNetwork.poolFromSeed(1000, 1L)
      case Some(pool) =>
        val filtered = pool.filter(filter1)
        println(s"${filtered.size} genes survived filter")
        val bred = NeuronalNetwork.breed(filtered, 1000, iteration.toLong)
        println(s"breeding gene pool to ${bred.size}")
        bred
    }

    repo.save(nextState)
    iteration += 1
  }
}
