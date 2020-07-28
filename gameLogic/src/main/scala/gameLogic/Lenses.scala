package gameLogic

import gameEntities._
import monocle.Optional
import monocle.function.Each.each
import monocle.macros.GenLens
import monocle.unsafe.UnsafeSelect

object Lenses {
  val cycle = GenLens[Game](_.cycle)

  val players    = GenLens[Game](_.players)
  val eachPlayer = players.composeTraversal(each)

  val runningPlayers  = eachPlayer.composeOptional(PlayerLenses.running)
  val finishedPlayers = eachPlayer.composeOptional(PlayerLenses.running)

  val scenario = GenLens[Game](_.scenario)

  val events = GenLens[Game](_.events)

  def player(id: String) =
    eachPlayer
      .composePrism(UnsafeSelect.unsafeSelect(_.id == id))

  def runningPlayer(id: String) =
    runningPlayers
      .composePrism(UnsafeSelect.unsafeSelect(_.id == id))

  def instructionSlots(playerName: String) = runningPlayer(playerName) composeLens PlayerLenses.instructionSlots

  def robot(playerName: String) = runningPlayer(playerName) composeLens PlayerLenses.robot

  def direction(playerName: String) = robot(playerName) composeLens RobotLenses.direction
  def position(playerName: String)  = robot(playerName) composeLens RobotLenses.position

  def log(entry: EventLog) = events.modify(_ :+ entry)
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

  val robot = GenLens[RunningPlayer](_.robot)

  val instructionSlots = GenLens[RunningPlayer](_.instructionSlots)

  val instructionOptions = GenLens[RunningPlayer](_.instructionOptions)
}

object RobotLenses {
  val direction = GenLens[Robot](_.direction)
  val position  = GenLens[Robot](_.position)
}
