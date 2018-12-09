package controller

import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}

class SinkSourceCache[A] {
  type Pair = (Sink[A, _], Source[A, _])

  private var memory = Map[String, Pair]()

  def createPair()(implicit mat: Materializer): Pair = MergeHub.source[A](perProducerBufferSize = 16)
    .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
    .run()

  def pair(id: String)(implicit mat: Materializer): Pair = {
    if(!memory.isDefinedAt(id))
      memory += id -> createPair()
    memory(id)
  }

  def sink(id: String)(implicit mat: Materializer): Sink[A, _] = pair(id)._1

  def source(id: String)(implicit mat: Materializer): Source[A, _] = pair(id)._2
}
