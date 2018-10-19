package roboRace.ai

import breeze.linalg.{DenseMatrix, DenseVector}
import gameLogic._
import gameLogic.gameUpdate.ScenarioEffects
import gameLogic.Constants._
import roboRace.ai.NeuronalNetwork._

import scala.annotation.tailrec
import scala.util.Random

case class NeuronalNetwork(weights: NeuronalNetworkWeights) extends Bot {
  import NeuronalNetwork._

  def extractInput(scenario: Scenario, otherRobots: Seq[Robot])
                  (thisRobot: Robot, instructionOptions: Seq[Instruction]): Seq[Double] = {
    val view = enumerateView(thisRobot.position, instructionsPerCycle * 2).toSeq
    val pits = view.map(ScenarioEffects.isPit(scenario, _))
    val distances = view.map(p => (scenario.targetPosition.x - p.x).abs + (scenario.targetPosition.y - p.y).abs)

    val instruction = for {
      i <- instructionOptions
      bools <- Instruction.instructions.map(_ == i)
    } yield bools

    def boolsToWeights(s:Seq[Boolean]): Seq[Double] = s.map(if(_) 1d else 0d)
    def intsToWeights(s:Seq[Int]): Seq[Double] = s.map(_.toDouble)

    boolsToWeights(pits)
      .++(intsToWeights(distances))
      .++(boolsToWeights(instruction))
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
}


case class NeuronalNetworkWeights(weights: Vector[Double], offsets: Vector[Double]) {
  val A: DenseMatrix[Double] = DenseMatrix.create(outputSize, inputSize, weights.toArray)
  val y: DenseVector[Double] = DenseVector(offsets.toArray)
}

object NeuronalNetworkWeights{
  def fromSeed(seed: Long): NeuronalNetworkWeights = {
    val r = new Random(seed)
    val weights = Vector.fill(NeuronalNetwork.weightsSize)(r.nextDouble())
    val offsets = Vector.fill(NeuronalNetwork.outputSize)(r.nextDouble())
    NeuronalNetworkWeights(weights, offsets)
  }
}