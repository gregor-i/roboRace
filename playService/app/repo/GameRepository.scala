package repo

import java.io.FileWriter

import gameLogic.GameState
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.io.Source
import scala.util.Try

trait GameRepository {
  def get(id: String): Option[GameState]
  def list(): Seq[(String, GameState)]
  def save(id: String, gameState: GameState): Unit
  def delete(id: String): Unit
}


class MemoryGameRepository extends GameRepository {
  def get(id: String): Option[GameState] = synchronized(cache.get(id))
  def list(): Seq[(String, GameState)] = synchronized(cache.toSeq)
  def save(id: String, gameState: GameState): Unit = synchronized{cache = cache + (id -> gameState)}
  def delete(id: String): Unit = synchronized{cache = cache - id}

  private[this] var cache: Map[String, GameState] = Map.empty
}

class FileGameRepository extends GameRepository{
  def get(id: String): Option[GameState] = synchronized(read().get(id))
  def list(): Seq[(String, GameState)] = synchronized(read().toSeq)
  def save(id: String, gameState: GameState): Unit = synchronized(write(read() + (id -> gameState)))
  def delete(id: String) : Unit = synchronized(write(read() - id))

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
