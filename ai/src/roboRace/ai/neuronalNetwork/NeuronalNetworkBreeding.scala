package roboRace.ai.neuronalNetwork

import scala.util.Random

object NeuronalNetworkBreeding {
  val initial = NeuronalNetwork(Seq.empty)

  def mutate(genes: NeuronalNetwork)(seed: Long): NeuronalNetwork = {
    val r = new Random(seed)

    def oneOf[T](s: Iterable[T]): T = s.iterator.drop(r.nextInt(s.size)).next

    def sliceOne[T](s: Seq[T]): (T, Seq[T]) = {
      val n = r.nextInt(s.size - 1)
      val (s1, s2) = s.splitAt(n)
      (s2.head, s1 ++ s2.tail)
    }

    val randomAction = r.nextInt(4)
    val newConnections = if (randomAction == 0 || genes.connections.length < 2) {
      genes.connections :+ NeuronConnection(oneOf(NeuronalNetworkInput.inputKeys), oneOf(NeuronalNetworkOutput.outputKeys), r.nextGaussian(), r.nextGaussian())
    } else {
      val (toMutate, rest) = sliceOne(genes.connections)
      randomAction match {
        case 1 =>
          rest
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