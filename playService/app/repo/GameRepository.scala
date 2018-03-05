package repo

import java.io.FileWriter
import javax.inject.Singleton

import gameLogic.{GameNotDefined, GameState}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.io.Source
import scala.util.Try


@Singleton
class GameRepository {
  def get(id: String): Option[GameState] = read().get(id)
  def list(): Seq[(String, GameState)] = read().toSeq
  def save(id: String, gameState: GameState): Unit = write(read() + (id -> gameState))

  if(get("default").isEmpty)
    save("default", GameNotDefined)

  private def file = new java.io.File("gameRepo.json")
  private def read(): Map[String, GameState] =
    Try {
      parse(Source.fromFile(file).mkString)
        .flatMap(_.as[Map[String, GameState]])
        .toOption
    }.toOption.flatten
      .getOrElse(Map.empty)
  private def write(state: Map[String, GameState]): Unit = {
    val writer = new FileWriter(file)
    writer.append(state.asJson.spaces2)
    writer.close()
  }
}
