package controller

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub}

object SinkSourceCache {
  type Sink = akka.stream.scaladsl.Sink[String, NotUsed]
  type Source = akka.stream.scaladsl.Source[String, NotUsed]
  type Pair = (Sink, Source)

  private var memory = Map[String, Pair]()

  def createPair()(implicit mat: Materializer): Pair = MergeHub.source[String](perProducerBufferSize = 16)
    .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
    .run()

  def pair(id: String)(implicit mat: Materializer): Pair = {
    if(!memory.isDefinedAt(id))
      memory += id -> createPair()
    memory(id)
  }

  def sink(id: String)(implicit mat: Materializer): Sink = pair(id)._1

  def source(id: String)(implicit mat: Materializer): Source = pair(id)._2
}