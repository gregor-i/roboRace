package roboRace.ai

import gameLogic.util.PathFinding
import gameLogic.{Game, _}
import gameLogic.gameUpdate.DealOptions
import monocle.function.Each

object Console extends App {
  val repo = new NeuronalNetworkRepository("neuronal-network-genes.json")

  val scenario = Scenario(10, 10, Position(0, 0), Seq(Robot(Position(9, 9), Up)), Seq.empty, Seq.empty, Seq.empty)
  val pathing = PathFinding.toTarget(scenario)

  def fitnessFunction(genes: NeuronalNetworkGenes): Double = {
    val bot = NeuronalNetwork(genes)
    val gameAfter1 = BotFinishesGame.sequenceWithAutoCycle(BotFinishesGame.createGame(scenario)("bot"))(
      BotFinishesGame.botInstructions(bot, "bot"),
      Game.players.composeTraversal(Each.each).composeLens(Player.instructionOptions).set(DealOptions.initial),
      BotFinishesGame.botInstructions(bot, "bot"),
      Game.players.composeTraversal(Each.each).composeLens(Player.instructionOptions).set(DealOptions.initial),
      BotFinishesGame.botInstructions(bot, "bot")
    )
    assert(gameAfter1.cycle == 1)
    val botPlayer = gameAfter1.players.head
    if(botPlayer.finished.isDefined) {
      0d
    }else{
      pathing(botPlayer.robot.position).length
    }
  }

  var iteration = 0
  var state: Option[Seq[NeuronalNetworkGenes]] = null
  while(true){
    println(s"Iteration $iteration:")
    if(state == null) {
      println("reading state from disk")
      state = repo.read()
    }
    println(s"current gene pool size: ${state.map(_.size)}")

    val nextState = state match {
      case None =>
        println("initializing gene pool")
        NeuronalNetwork.poolFromSeed(1000, 1L)
      case Some(pool) =>
        println("calculating fitness")
        val genesWithFitness = pool.map(genes => (genes, fitnessFunction(genes)))
        val genesGrouped = genesWithFitness.groupBy(_._2).mapValues(_.map(_._1)).toSeq.sortBy(_._1)
        println(s"Gene pool grouped by fitness: ${genesGrouped.map(t => (t._1, t._2.size))}")
        val bestGroup = genesGrouped.head._2
        val bred = NeuronalNetwork.breed(bestGroup, Math.min(bestGroup.size * 2, 1000), iteration.toLong)
        println(s"breeding gene pool to ${bred.size}")
        bred
    }

    println("writing state to disk")
    repo.save(nextState)
    iteration += 1
  }
}
