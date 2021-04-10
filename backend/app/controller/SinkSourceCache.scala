package controller

import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global

class SinkSourceCache[A] {
  type Pair = (Sink[A, _], Source[A, _])

  private var memory = Map[String, Pair]()

  def createPair()(implicit mat: Materializer): Pair =
    MergeHub
      .source[A](perProducerBufferSize = 1)
      .watchTermination() { (mat, fut) =>
        fut.foreach(_ => println("source terminated!"))
        mat
      }
      .toMat(BroadcastHub.sink(bufferSize = 1))(Keep.both)
      .run()

  def pair(id: String)(implicit mat: Materializer): Pair = {
    if (!memory.isDefinedAt(id))
      memory += id -> createPair()
    memory(id)
  }

  def sink(id: String)(implicit mat: Materializer): Sink[A, _] = pair(id)._1

  def source(id: String)(implicit mat: Materializer): Source[A, _] = pair(id)._2
}
