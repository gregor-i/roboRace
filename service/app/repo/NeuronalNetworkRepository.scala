package repo

import java.io.FileWriter

import io.circe.Json
import javax.inject.{Inject, Singleton}
import roboRace.ai.NeuronalNetworkGenes
import io.circe.generic.auto._
import io.circe.syntax._

import scala.io.Source
import io.circe._
import io.circe.parser._

import scala.util.Try


@Singleton
class NeuronalNetworkRepository @Inject()() {
  val file = new java.io.File("neuronal-network-genes.json")

  def save(genes: Seq[NeuronalNetworkGenes]) = {
    val writer = new FileWriter(file)
    writer.append(genes.asJson.pretty(Printer.noSpaces))
    writer.close()
  }

  def read(): Option[Seq[NeuronalNetworkGenes]] = Try(parse(Source.fromFile(file).mkString).right.flatMap(_.as[Seq[NeuronalNetworkGenes]]).toOption).toOption.flatten
}
