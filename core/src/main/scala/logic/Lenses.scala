package logic

import entities._
import monocle.Optional
import monocle.function.Each
import monocle.unsafe.UnsafeSelect

object Lenses {
  val eachPlayer = Game.players.composeTraversal(Each.each(Each.listEach[Player]))

  val runningPlayers  = eachPlayer.composeOptional(PlayerLenses.running)
  val finishedPlayers = eachPlayer.composeOptional(PlayerLenses.running)

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

  def log(entry: EventLog) = Game.events.modify(_ :+ entry)
}

object PlayerLenses {
  val running = Optional[Player, RunningPlayer] {
    case r: RunningPlayer => Some(r)
    case _                => None
  }(s => _ => s)

  val finished = Optional[Player, FinishedPlayer] {
    case r: FinishedPlayer => Some(r)
    case _                 => None
  }(s => _ => s)
}
