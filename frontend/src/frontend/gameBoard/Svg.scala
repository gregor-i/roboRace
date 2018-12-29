package frontend.gameBoard

import com.raquo.snabbdom.simple.implicits._
import com.raquo.snabbdom.simple.{VNode, attrs, tags}
import com.raquo.snabbdom.simple.styles.visibility
import com.raquo.snabbdom.simple.events.onClick
import frontend.common.Images
import frontend.util.Ui
import gameEntities._
import com.raquo.snabbdom.simple.attrs.id
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}

object Svg extends Ui {
  val deltaLeft = 0.75
  val deltaTop = Math.sqrt(3) / 2


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

  def target(scenario: Scenario): VNode =
    tags.build("image")(
      attrs.build[String]("href") := Images.target,
      attrs.build[String]("width") := "1",
      attrs.build[String]("height") := "1",
      attrs.build[String]("x") := left(scenario.targetPosition).toString,
      attrs.build[String]("y") := top(scenario.targetPosition).toString,
    )

  def tiles(scenario: Scenario, click: Option[(Position, Direction) => Unit]): Seq[VNode] =
    for {
      x <- 0 until scenario.width
      y <- 0 until scenario.height
      p = Position(x, y)
    } yield tags.build("image")(
      attrs.build[String]("href") := Images.tile,
      attrs.build[String]("width") := "1",
      attrs.build[String]("height") := "1",
      attrs.build[String]("x") := left(p).toString,
      attrs.build[String]("y") := top(p).toString,
      if (scenario.pits.contains(p)) visibility := "hidden" else None,
      click.map(f => onClick := (event => f(p, event2direction(event))))
    )

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

  def walls(scenario: Scenario): Seq[VNode] =
    scenario.walls.map(wall =>
      tags.build("image")(
        attrs.build[String]("href") := Images.wall(wall.direction),
        attrs.build[String]("width") := "1",
        attrs.build[String]("height") := "1",
        attrs.build[String]("x") := left(wall.position).toString,
        attrs.build[String]("y") := top(wall.position).toString,
      )
    )

  def traps(scenario: Scenario): Seq[VNode] =
    scenario.traps.map(trap =>
      tags.build("image")(
        attrs.build[String]("href") := Images.trap(trap),
        attrs.build[String]("width") := "1",
        attrs.build[String]("height") := "1",
        attrs.build[String]("x") := left(trap.position).toString,
        attrs.build[String]("y") := top(trap.position).toString,
      )
    )

  def startPoints(scenario: Scenario): Seq[VNode] =
    scenario.initialRobots.map(robot =>
      tags.build("image")(
        attrs.build[String]("href") := Images.playerStart(robot.index),
        attrs.build[String]("transform") := translate(robot.position)
          + " translate(0.5 0.5) "
          + s"rotate(${directionToRotation(robot.direction)})"
          + " translate(-0.5 -0.5)",
        attrs.build[String]("width") := "1",
        attrs.build[String]("height") := "1",
        attrs.build[String]("x") := "0",
        attrs.build[String]("y") := "0",
      )
    )

  def robots(robots: Seq[Robot]): Seq[VNode] = {
    robots.map(robot =>
      tags.build("g")(
        attrs.build[String]("transform") := "translate(0.5 0.5)",
        tags.build("g")(
          id := s"robot-translation-${robot.index}",
          attrs.build[String]("transform") := translate(robot.position),
          tags.build("g")(
            id := s"robot-rotation-${robot.index}",
            attrs.build[String]("transform") := s"rotate(${directionToRotation(robot.direction)})",
            tags.build("g")(
              id := s"robot-scale-${robot.index}",
              attrs.build[String]("transform") := s"scale(1)",
              tags.build("g")(
                attrs.build[String]("transform") := "translate(-0.5 -0.5)",
                tags.build("image")(
                  attrs.build[String]("href") := Images.player(robot.index),
                  attrs.build[String]("width") := "1",
                  attrs.build[String]("height") := "1",
                  attrs.build[String]("x") := "0",
                  attrs.build[String]("y") := "0",
                )
              )
            )
          )
        )
      )
    )
  }
}
