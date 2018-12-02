package roboRace.ai

import gameLogic._
import gameLogic.util.PathFinding
import io.circe.generic.auto._
import roboRace.ai.neuronalNetwork.{JsonRepository, NeuronalNetwork, NeuronalNetworkBreeding}

import scala.util.Random

object Console {
  val geneRepo = new JsonRepository[Seq[NeuronalNetwork]]("neuronal-network-genes.json")
  val bestGeneRepo = new JsonRepository[NeuronalNetwork]("best-neuronal-network.json")

  val scenario = Scenario(10, 10, Position(0, 0), Seq(Robot(Position(9, 9), Up)), Seq.empty, Seq.empty, Seq.empty)
  val pathing = PathFinding.toTarget(scenario)
  val r = new Random(0)

  def fitnessFunction(bot: Bot): Double = {
    val gameAfter1 = BotFinishesGame.sequenceWithAutoCycle(BotFinishesGame.createGame(scenario)("bot"))(
      BotFinishesGame.botInstructions(bot, "bot"),
      BotFinishesGame.botInstructions(bot, "bot"),
      BotFinishesGame.botInstructions(bot, "bot"),
      BotFinishesGame.botInstructions(bot, "bot"),
      BotFinishesGame.botInstructions(bot, "bot")
    )
    val botPlayer = gameAfter1.players.head
    if (botPlayer.finished.isDefined) {
      botPlayer.finished.get.cycle
    } else {
      (pathing(botPlayer.robot.position).length +1 ) * 10
    }
  }

  def main(args: Array[String]): Unit = {
    var iteration = 0
    var state: Option[Seq[NeuronalNetwork]] = None
    if (state.isEmpty) {
      println("reading state from disk")
      state = geneRepo.read()
    }

    while (true) {
      println(s"Iteration $iteration:")
      println(s"current gene pool size: ${state.map(_.size)}")

      val nextState = state match {
        case None =>
          println("initializing gene pool")
          Seq.tabulate(10000)(seed => NeuronalNetworkBreeding.mutate(NeuronalNetworkBreeding.initial)(seed))
        case Some(pool) =>
          println("calculating fitness")
          val genesWithFitness = pool.map(genes => (genes, fitnessFunction(genes)))
          val genesGrouped = genesWithFitness.groupBy(_._2).mapValues(_.map(_._1)).toSeq.sortBy(_._1)
          println(s"Gene pool grouped by fitness: ${genesGrouped.map(t => (t._1, t._2.size))}")
          val bred = genesGrouped.flatMap(_._2).take(2000).flatMap(network => Seq(
            network,
            NeuronalNetworkBreeding.mutate(network)(r.nextLong()),
            NeuronalNetworkBreeding.mutate(network)(r.nextLong()),
            NeuronalNetworkBreeding.mutate(network)(r.nextLong()),
            NeuronalNetworkBreeding.mutate(network)(r.nextLong())
          ))
          if (genesGrouped.head._1 == 0 && genesGrouped.head._2.size > 1000) {
            println("more then 1000 bots finished level. stopped breeding")
            return
          }

          bestGeneRepo.save(genesGrouped.head._2.head)

          println(s"breeding gene pool to ${bred.size}")
          bred
      }

      println("writing state to disk")
      state = Some(nextState)
      geneRepo.save(nextState)
      iteration += 1
    }
  }
}
