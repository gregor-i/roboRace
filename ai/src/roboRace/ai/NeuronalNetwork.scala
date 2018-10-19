package roboRace.ai

import breeze.linalg.{DenseMatrix, DenseVector}
import gameLogic._
import gameLogic.gameUpdate.ScenarioEffects
import gameLogic.Constants._

import scala.annotation.tailrec
import scala.util.Random

case class NeuronalNetwork(weights: NeuronalNetworkGens) extends Bot {
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

object NeuronalNetwork{
  val viewDistance = instructionsPerCycle * 2
  val viewSize = enumeratedViewSize(viewDistance)
  val inputSize =  viewSize * 2 + instructionOptionsPerCycle * Instruction.instructions.size
  val outputSize = instructionOptionsPerCycle * 2
  val weightsSize = inputSize * outputSize

  def enumerateView(center: Position, distance: Int): Seq[Position] = {
    @tailrec
    def ntimes(direction: Direction, n: Int)(p: Position) : Position =
      if(n == 0)
        p
      else ntimes(direction, n-1)(direction(p))

    if(distance >= 0)
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
    val orderWeight = output.slice(n, n *2)

    (0 until n)
      .map(i => (i, activationWeight(i), orderWeight(i)))
      .sortBy(_._2)
      .take(instructionsPerCycle)
      .sortBy(_._3)
      .map(_._1)
  }

  def genesFromSeed(seed: Long): NeuronalNetworkGens = {
    val r = new Random(seed)
    val weights = Vector.fill(NeuronalNetwork.weightsSize)(r.nextDouble())
    val offsets = Vector.fill(NeuronalNetwork.outputSize)(r.nextDouble())
    NeuronalNetworkGens(weights, offsets)
  }

  @tailrec
  def breed(genePool: Seq[NeuronalNetworkGens], newSize: Int, seed: Long): Seq[NeuronalNetworkGens] = {
    if(genePool.size >= newSize)
      genePool
    else {
      val r = new Random(seed)
      val p1 = genePool(r.nextInt(genePool.size))
      val p2 = genePool(r.nextInt(genePool.size))
      val p3 = genesFromSeed(r.nextLong())

      val f1 = r.nextGaussian()
      val f2 = r.nextGaussian()
      val f3 = r.nextGaussian()
      val f0 = f1 + f2 + f3

      val newWeights = p1.weights.zip(p2.weights).zip(p3.weights).map{
        case ((g1, g2), g3) => (g1 * f1 + f2 * f2 + g3 * f3) / f0
      }
      val newOffsets = p1.offsets.zip(p2.offsets).zip(p3.offsets).map{
        case ((g1, g2), g3) => (g1 * f1 + f2 * f2 + g3 * f3) / f0
      }
      val newGenes = NeuronalNetworkGens(newWeights, newOffsets)
      breed(genePool :+ newGenes, newSize, r.nextLong())
    }
  }
}


case class NeuronalNetworkGens(weights: Vector[Double], offsets: Vector[Double]) {
  val A: DenseMatrix[Double] = DenseMatrix.create(NeuronalNetwork.outputSize, NeuronalNetwork.inputSize, weights.toArray)
  val y: DenseVector[Double] = DenseVector(offsets.toArray)
}