package roboRace.ai.neuronalNetwork

import scala.util.Random

object NeuronalNetworkBreeding {
  val initial = NeuronalNetwork(Seq.empty)

  private def oneOf[T](s: Iterable[T])(seed: Long): T = s.iterator.drop(new Random(seed).nextInt(s.size)).next

  private def sliceOne[T](s: Seq[T])(seed: Long): (T, Seq[T]) = {
    val n = new Random(seed).nextInt(s.size - 1)
    val (s1, s2) = s.splitAt(n)
    (s2.head, s1 ++ s2.tail)
  }

  def addConnection(connections: Seq[NeuronConnection])(seed: Long): Seq[NeuronConnection] = {
    val r = new Random(seed)
    connections :+ NeuronConnection(oneOf(NeuronalNetworkInput.inputKeys)(r.nextLong()), oneOf(NeuronalNetworkOutput.outputKeys)(r.nextLong()), r.nextGaussian(), r.nextGaussian())
  }

  def removeConnection(connections: Seq[NeuronConnection])(seed: Long): Seq[NeuronConnection] = {
    val r = new Random(seed)
    if (connections.isEmpty) {
      Seq.empty
    } else {
      val (toMutate, rest) = sliceOne(connections)(r.nextLong())
      rest
    }
  }

  def mutate(genes: NeuronalNetwork)(seed: Long): NeuronalNetwork = {
    val r = new Random(seed)

    val randomAction = r.nextInt(4)
    val newConnections = if (randomAction == 0 || genes.connections.length < 2) {
      addConnection(genes.connections)(r.nextLong())
    } else {
      val (toMutate, rest) = sliceOne(genes.connections)(r.nextLong())
      randomAction match {
        case 1 =>
          removeConnection(genes.connections)(r.nextLong())
        case 2 =>
          rest :+ toMutate.copy(weight = toMutate.weight + r.nextGaussian())
        case 3 =>
          rest :+ toMutate.copy(bias = toMutate.bias + r.nextGaussian())
      }
    }
    NeuronalNetwork(newConnections)
  }

  def interbreed(p1: NeuronalNetwork, p2: NeuronalNetwork)(seed: Long): NeuronalNetwork = ???

}