package svg

import gameLogic._

object SVGRenderingConstants {
  val tile = 50

  val deltaLeft = 0.75
  val deltaTop = Math.sqrt(3) / 2

  def left(x: Int, y: Int): Double = 0.75 * x

  def top(x: Int, y: Int): Double = deltaTop * (y + (x % 2).toDouble / 2)

  def height(scenario: Scenario): Double = (top(0, scenario.height) + 0.5) * tile

  def width(scenario: Scenario): Double = (left(scenario.width, 0) + (1-deltaLeft)) * tile

  def robotColor(index: Int): String =
    ((index % 6 + 6) % 6) match {
      case 0 => "blue"
      case 1 => "green"
      case 2 => "red"
      case 3 => "orange"
      case 4 => "cyan"
      case 5 => "magenta"
    }

  def directionToRotation(dir: Direction): Int = dir match {
    case Up => 0
    case UpRight => 60
    case DownRight => 120
    case Down => 180
    case DownLeft => 240
    case UpLeft => 300
  }
}
