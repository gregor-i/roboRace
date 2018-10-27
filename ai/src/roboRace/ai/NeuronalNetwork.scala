package roboRace.ai

import breeze.linalg.{DenseMatrix, DenseVector}
import gameLogic._
import gameLogic.gameUpdate.ScenarioEffects
import gameLogic.Constants._

import scala.annotation.tailrec
import scala.util.Random

case class NeuronalNetwork(weights: NeuronalNetworkGenes) extends Bot {

  import NeuronalNetwork.{extractInput, outputToInstructions}

  def neuronalNetwork(input: Seq[Double]): Seq[Double] = {
    val x = DenseVector(input.toArray)
    (weights.A * x + weights.y).data
  }

  def apply(scenario: Scenario, otherRobots: Seq[Robot])
           (thisRobot: Robot, instructionOptions: Seq[Instruction]): Seq[Int] = {
    val input = extractInput(scenario, otherRobots)(thisRobot, instructionOptions)
    val output = neuronalNetwork(input)
    outputToInstructions(output)
  }
}

object NeuronalNetwork {
  val viewDistance = instructionsPerCycle * 2
  val viewSize = enumeratedViewSize(viewDistance)
  val inputSize = viewSize * 2 + instructionOptionsPerCycle * Instruction.instructions.size
  val outputSize = instructionOptionsPerCycle * 2
  val weightsSize = inputSize * outputSize

  def enumerateView(center: Position, distance: Int): Seq[Position] = {
    @tailrec
    def ntimes(direction: Direction, n: Int)(p: Position): Position =
      if (n == 0)
        p
      else ntimes(direction, n - 1)(direction(p))

    if (distance >= 0)
      center +: (for {
        d <- Direction.directions
        rd = d.right
        i <- 1 to distance
        j <- 0 to distance - i
      } yield ntimes(rd, j)(ntimes(d, i)(center)))
    else
      Seq.empty
  }

  // https://de.wikipedia.org/wiki/Zentrierte_Sechseckszahl
  def enumeratedViewSize(_n: Int): Int = {
    val n = _n + 1
    3 * n * n - 3 * n + 1
  }

  def extractInput(scenario: Scenario, otherRobots: Seq[Robot])
                  (thisRobot: Robot, instructionOptions: Seq[Instruction]): Seq[Double] = {
    def boolsToWeights(s: Seq[Boolean]): Seq[Double] = s.map(if (_) 1d else 0d)

    def intsToWeights(s: Seq[Int]): Seq[Double] = s.map(_.toDouble)

    val view = enumerateView(thisRobot.position, viewDistance)

    val pits = boolsToWeights(view.map(ScenarioEffects.isPit(scenario, _))) ensuring (_.size == viewSize)
    val distances = intsToWeights(view.map(p => (scenario.targetPosition.x - p.x).abs + (scenario.targetPosition.y - p.y).abs)) ensuring (_.size == viewSize)
    val instruction = boolsToWeights(for {
      i <- instructionOptions
      bools <- Instruction.instructions.map(_ == i)
    } yield bools) ensuring (_.size == instructionOptionsPerCycle * Instruction.instructions.size)


    Seq(
      pits,
      distances,
      instruction
    ).flatten
  }

  def outputToInstructions(output: Seq[Double]): Seq[Int] = {
    val n = instructionOptionsPerCycle
    assert(output.size == NeuronalNetwork.outputSize)
    val activationWeight = output.take(n)
    val orderWeight = output.slice(n, n * 2)

    (0 until n)
      .map(i => (i, activationWeight(i), orderWeight(i)))
      .sortBy(_._2)
      .take(instructionsPerCycle)
      .sortBy(_._3)
      .map(_._1)
  }

  def genesFromSeed(seed: Long): NeuronalNetworkGenes = {
    val r = new Random(seed)
    val weights = Vector.fill(NeuronalNetwork.weightsSize)(r.nextDouble())
    val offsets = Vector.fill(NeuronalNetwork.outputSize)(r.nextDouble())
    NeuronalNetworkGenes(weights, offsets)
  }

  def genesFromPool(genes: Seq[NeuronalNetworkGenes], seed: Long): NeuronalNetworkGenes = {
    val r = new Random(seed)
    val p1 = genes(r.nextInt(genes.size))
    val p2 = genes(r.nextInt(genes.size))
    val p3 = genesFromSeed(r.nextLong())

    val f1 = r.nextGaussian()
    val f2 = r.nextGaussian()
    val f3 = r.nextGaussian() * 0.1
    val f0 = f1 + f2 + f3

    val newWeights = Vector.tabulate(p1.weights.size) {
      i => (p1.weights(i) * f1 + p2.weights(i) * f2 + p3.weights(i) * f3) / f0
    }

    val newOffsets = Vector.tabulate(p1.offsets.size) {
      i => (p1.offsets(i) * f1 + p2.offsets(i) * f2 + p3.offsets(i) * f3) / f0
    }
    NeuronalNetworkGenes(newWeights, newOffsets)
  }

  def breed(genePool: Seq[NeuronalNetworkGenes], newSize: Int, seed: Long): Seq[NeuronalNetworkGenes] = {
    val r = new Random(seed)
    Seq.fill(newSize)(r.nextLong())
      .par
      .map(genesFromPool(genePool, _))
      .seq
  }

  def grow(genePool: Seq[NeuronalNetworkGenes], newSize: Int, seed: Long, filter: NeuronalNetworkGenes => Boolean): Seq[NeuronalNetworkGenes] = {
    val r = new Random(seed)
    @tailrec
    def loop(pool: Seq[NeuronalNetworkGenes]): Seq[NeuronalNetworkGenes] =
      if(pool.size < newSize) {
        val newGenes = genesFromPool(pool, r.nextLong())
        if(filter(newGenes))
          loop(pool :+ newGenes)
        else
          loop(pool)
      }else{
        pool
      }

    loop(genePool.filter(filter))
  }
}


case class NeuronalNetworkGenes(weights: Vector[Double], offsets: Vector[Double]) {
  val A: DenseMatrix[Double] = DenseMatrix.create(NeuronalNetwork.outputSize, NeuronalNetwork.inputSize, weights.toArray)
  val y: DenseVector[Double] = DenseVector(offsets.toArray)
}