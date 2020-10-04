package roborace.frontend.pages.components.gameBoard

import entities._
import roborace.frontend.pages.components.gameBoard.Svg._
import snabbdom.Node

object Animation {

  def nearestRotationTarget(from: Double, to: Double): Double = {
    val a = Math.abs(from - to)
    val b = Math.abs(from - to + 360)
    val c = Math.abs(from - to - 360)
    if (a < b && a < c)
      to
    else if (b < c)
      to - 360
    else
      to + 360
  }

  def eventDuration(event: EventLog): Double =
    event match {
      case _: RobotReset       => 1.0
      case _: RobotTurns       => 0.5
      case _: RobotMoves       => 0.5
      case _: PlayerJoinedGame => 0.5
      case _: PlayerQuitted    => 0.5
      case _: PlayerFinished   => 0.5
      case _: TrapEffect       => 0.5
      case _                   => 0.0
    }

  def eventSequenceDuration(events: Seq[EventLog]): Double =
    events.map(eventDuration).sum

  def animateRotation(playerIndex: Int, from: Direction, to: Direction, begin: Double, duration: Double): Node =
    Node("animateTransform")
      .attrs(
        Seq(
          "attributeName" -> "transform",
          "type"          -> "rotate",
          "href"          -> s"#robot-rotation-${playerIndex}",
          "from"          -> directionToRotation(from).toString,
          "to"            -> nearestRotationTarget(directionToRotation(from), directionToRotation(to)).toString,
          "begin"         -> s"${begin}s",
          "dur"           -> s"${duration}s",
          "fill"          -> "freeze"
        )
      )

  def animateTranslation(playerIndex: Int, from: Position, to: Position, begin: Double, duration: Double): Node =
    Node("animateTransform").attrs(
      Seq(
        "attributeName" -> "transform",
        "type"          -> "translate",
        "href"          -> s"#robot-translation-${playerIndex}",
        "from"          -> s"${left(from)} ${top(from)}",
        "to"            -> s"${left(to)} ${top(to)}",
        "begin"         -> s"${begin}s",
        "dur"           -> s"${duration}s",
        "fill"          -> "freeze"
      )
    )

  def animateScale(playerIndex: Int, from: Double, to: Double, begin: Double, duration: Double): Node =
    Node("animateTransform").attrs(
      Seq(
        "attributeName" -> "transform",
        "type"          -> "scale",
        "href"          -> s"#robot-scale-${playerIndex}",
        "from"          -> from.toString,
        "to"            -> to.toString,
        "begin"         -> s"${begin}s",
        "dur"           -> s"${duration}s",
        "fill"          -> "freeze"
      )
    )

  def animateSpawn(playerIndex: Int, robot: Robot, begin: Double, duration: Double) = Seq(
    animateRotation(playerIndex, robot.direction, robot.direction, begin, 0),
    animateTranslation(playerIndex, robot.position, robot.position, begin, 0),
    animateScale(playerIndex, 0, 1, begin, duration)
  )

  def animateDespawn(playerIndex: Int, robot: Robot, begin: Double, duration: Double) = Seq(
    animateRotation(playerIndex, robot.direction, robot.direction, begin, 0),
    animateTranslation(playerIndex, robot.position, robot.position, begin, 0),
    animateScale(playerIndex, 1, 0, begin, duration)
  )

  def animations(events: Seq[EventLog]): Seq[Node] =
    events
      .foldLeft((Seq.empty[Node], 0d)) { (t, event) =>
        val (acc, startTime) = t
        val duration         = eventDuration(event)
        val animations = event match {
          case e: RobotTurns => Seq(animateRotation(e.playerIndex, e.from, e.to, startTime, duration))

          case e: RobotMoves =>
            e.transitions.map(transition => animateTranslation(transition.playerIndex, transition.from, transition.to, startTime, duration))

          case e: RobotReset =>
            Seq(
              animateDespawn(e.playerIndex, e.from, startTime, duration / 2),
              animateSpawn(e.playerIndex, e.to, startTime + duration / 2, duration / 2)
            ).flatten

          case e: PlayerJoinedGame => animateSpawn(e.playerIndex, e.robot, startTime, duration)

          case e: PlayerQuitted => animateDespawn(e.playerIndex, e.robot, startTime, duration)

          case e: PlayerFinished => animateDespawn(e.playerIndex, e.robot, startTime, duration)

          case _ => Seq.empty[Node]
        }
        (acc ++ animations, startTime + duration)
      }
      ._1

}
