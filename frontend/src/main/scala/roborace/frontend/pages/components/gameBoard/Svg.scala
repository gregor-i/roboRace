package roborace.frontend.pages.components.gameBoard

import gameEntities._
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}
import roborace.frontend.pages.components.{Images, RobotColor}
import snabbdom.{Node, Snabbdom}

import scala.util.chaining._

object Svg {
  val deltaLeft = 0.75
  val deltaTop  = Math.sqrt(3) / 2

  def left(pos: Position): Double = deltaLeft * pos.x

  def top(pos: Position): Double = deltaTop * (pos.y + ((pos.x % 2d) + 2d) % 2d / 2d)

  def height(scenario: Scenario): Double = top(Position(0, scenario.height)) + 0.5

  def width(scenario: Scenario): Double = left(Position(scenario.width, 0)) + (1 - deltaLeft)

  def directionToRotation(dir: Direction) = dir match {
    case Up        => 0
    case UpRight   => 60
    case DownRight => 120
    case Down      => 180
    case DownLeft  => 240
    case UpLeft    => 300
  }

  def translate(pos: Position) =
    s"translate(${left(pos)} ${top(pos)})"

  def targets(scenario: Scenario, active: Option[Int]): Seq[Node] =
    for ((t, index) <- scenario.targets.zipWithIndex)
      yield Node("g")
        .child(
          Node("image")
            .attrs(
              Seq(
                "href"   -> (if (active.fold(true)(_ == index)) Images.target else Images.targetInactive),
                "width"  -> "1",
                "height" -> "1",
                "x"      -> left(t).toString,
                "y"      -> top(t).toString
              )
            )
        )
        .child(
          Node("text")
            .attrs(
              Seq(
                "x"     -> (left(t) + 0.6).toString,
                "y"     -> (top(t) + 0.75).toString,
                "style" -> "font-size:0.2px;stroke:none;",
                "fill"  -> (if (active.fold(true)(_ == index)) "#000" else "#999")
              )
            )
            .text((index + 1).toString)
        )

  def tiles(scenario: Scenario, click: Option[(Position, Direction) => Unit]): Seq[Node] =
    for {
      x <- 0 until scenario.width
      y <- 0 until scenario.height
      p = Position(x, y)
    } yield Node("image")
      .attrs(
        Seq(
          "href"   -> Images.tile,
          "width"  -> "1",
          "height" -> "1",
          "x"      -> left(p).toString,
          "y"      -> top(p).toString
        )
      )
      .style("visibility", if (scenario.pits.contains(p)) "hidden" else "")
      .pipe { node =>
        click match {
          case Some(f) =>
            node.event("click", Snabbdom.specificEvent[MouseEvent] { clickEvent =>
              f(p, event2direction(clickEvent))
            })
          case None => node
        }
      }

  private def event2direction(event: MouseEvent): Direction = {
    val bb = event.target.asInstanceOf[HTMLElement].getBoundingClientRect()
    val dx = event.pageX - bb.left - bb.width / 2
    val dy = event.pageY - bb.top - bb.height / 2
    Math.floor((Math.atan2(dy, dx) / Math.PI * 3 + 6) % 6) match {
      case 0 => DownRight
      case 1 => Down
      case 2 => DownLeft
      case 3 => UpLeft
      case 4 => Up
      case 5 => UpRight
    }
  }

  def walls(scenario: Scenario): Seq[Node] =
    scenario.walls.map(
      wall =>
        Node("image").attrs(
          Seq(
            "href"   -> Images.wall(wall.direction),
            "width"  -> "2",
            "height" -> "2",
            "x"      -> (left(wall.position) - 0.5).toString,
            "y"      -> (top(wall.position) - 0.5).toString
          )
        )
    )

  def traps(scenario: Scenario): Seq[Node] =
    scenario.traps.map(
      trap =>
        Node("image").attrs(
          Seq(
            "href"   -> Images.trap(trap),
            "width"  -> "1",
            "height" -> "1",
            "x"      -> left(trap.position).toString,
            "y"      -> top(trap.position).toString
          )
        )
    )

  def startPoints(scenario: Scenario): Seq[Node] =
    scenario.initialRobots.map(
      robot =>
        Node("image")
          .attrs(
            Seq(
              "href"      -> Images.playerStart(robot.index),
              "transform" -> (translate(robot.position) + " translate(0.5 0.5) " + s"rotate(${directionToRotation(robot.direction)})" + " translate(-0.5 -0.5)"),
              "width"     -> "1",
              "height"    -> "1",
              "x"         -> "0",
              "y"         -> "0"
            )
          )
          .style("color", RobotColor.dark(robot.index))
    )

  def robots(robots: Seq[Robot]): Seq[Node] = {
    robots.map(
      robot =>
        Node("g")
          .attr("transform", "translate(0.5 0.5)")
          .child(
            Node("g")
              .prop("id", s"robot-translation-${robot.index}")
              .attr("transform", translate(robot.position))
              .child(
                Node("g")
                  .prop("id", s"robot-rotation-${robot.index}")
                  .attr("transform", s"rotate(${directionToRotation(robot.direction)})")
                  .child(
                    Node("g")
                      .prop("id", s"robot-scale-${robot.index}")
                      .attr("transform", s"scale(0)")
                      .child(
                        Node("g")
                          .attr("transform", "translate(-0.5 -0.5)")
                          .child(
                            Node("image")
                              .attrs(
                                Seq(
                                  "href"   -> Images.player(robot.index),
                                  "width"  -> "1",
                                  "height" -> "1",
                                  "x"      -> "0",
                                  "y"      -> "0"
                                )
                              )
                          )
                      )
                  )
              )
          )
    )
  }
}
