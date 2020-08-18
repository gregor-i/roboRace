package logic

import entities._
import monocle.{Optional, Prism}
import monocle.function.Each
import monocle.unsafe.UnsafeSelect

object Lenses {
  val eachPlayer = Game.players.composeTraversal(Each.each(Each.listEach[Player]))

  val runningPlayers = eachPlayer.composePrism(PlayerLenses.running)

  def player(id: String) =
    eachPlayer
      .composePrism(UnsafeSelect.unsafeSelect(_.id == id))

  def runningPlayer(id: String) =
    runningPlayers
      .composePrism(UnsafeSelect.unsafeSelect(_.id == id))

  def instructionSlots(playerName: String) = runningPlayer(playerName) composeLens RunningPlayer.instructionSlots

  def robot(playerName: String) = runningPlayer(playerName) composeLens RunningPlayer.robot

  def direction(playerName: String) = robot(playerName) composeLens Robot.direction
  def position(playerName: String)  = robot(playerName) composeLens Robot.position
}

object PlayerLenses {
  val running = Prism[Player, RunningPlayer] {
    case r: RunningPlayer => Some(r)
    case _                => None
  }(s => s)
}
