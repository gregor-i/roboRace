package roboRace.ai.neuronalNetwork

import java.io.FileWriter

import io.circe._

import io.circe.parser._
import io.circe.syntax._

import scala.io.Source
import scala.util.Try

class JsonRepository[A: Decoder : Encoder](fileName: String) {
  val file = new java.io.File(fileName)

  def save(docs: A) = {
    val writer = new FileWriter(file)
    writer.append(docs.asJson.pretty(Printer.noSpaces))
    writer.close()
  }

  def read(): Option[A] = for {
    read <- Try(Source.fromFile(file).mkString).toOption
    parsed <- parse(read).toOption
    cast <- parsed.as[A].toOption
  } yield cast
}
