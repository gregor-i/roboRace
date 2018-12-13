package gameLogic

import gameEntities.{EventLog, Game, Player, Robot}
import monocle.function.Each.each
import monocle.macros.GenLens
import monocle.unsafe.UnsafeSelect

object Lenses {
  val cycle = GenLens[Game](_.cycle)

  val players = GenLens[Game](_.players)

  val scenario = GenLens[Game](_.scenario)

  val events = GenLens[Game](_.events)

  def player(name: String) = players.composeTraversal(each).composePrism(UnsafeSelect.unsafeSelect[Player](_.name == name))

  def instructionSlots(playerName: String) = player(playerName) composeLens PlayerLenses.instructionSlots
  def finished(playerName: String) = player(playerName) composeLens PlayerLenses.finished

  def robot(playerName: String) = player(playerName) composeLens PlayerLenses.robot

  def direction(playerName: String) = robot(playerName) composeLens RobotLenses.direction
  def position(playerName: String) = robot(playerName) composeLens RobotLenses.position

  def log(entry: EventLog) = events.modify(_ :+ entry)
}

object PlayerLenses {
  val robot = GenLens[Player](_.robot)

  val instructionSlots = GenLens[Player](_.instructionSlots)

  val instructionOptions = GenLens[Player](_.instructionOptions)

  val finished = GenLens[Player](_.finished)
}

object RobotLenses {
  val direction = GenLens[Robot](_.direction)
  val position = GenLens[Robot](_.position)
}
