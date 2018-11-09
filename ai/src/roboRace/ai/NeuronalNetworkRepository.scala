package roboRace.ai

import java.io.FileWriter

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.io.Source
import scala.util.Try

class NeuronalNetworkRepository(fileName:String) {
  val file = new java.io.File(fileName)

  def save(genes: Seq[NeuronalNetworkGenes]) = {
    val writer = new FileWriter(file)
    writer.append(genes.asJson.pretty(Printer.noSpaces))
    writer.close()
  }

  def read(): Option[Seq[NeuronalNetworkGenes]] = Try(parse(Source.fromFile(file).mkString).right.flatMap(_.as[Seq[NeuronalNetworkGenes]]).toOption).toOption.flatten
}
