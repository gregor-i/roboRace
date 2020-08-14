package logic

import entities._

object Direction {
  def move(dir: Direction, pos: Position): Position = {
    import pos.{x, y}
    dir match {
      case Up        => Position(x, y - 1)
      case UpRight   => if (x % 2 == 0) Position(x + 1, y - 1) else Position(x + 1, y)
      case DownRight => if (x % 2 == 0) Position(x + 1, y) else Position(x + 1, y + 1)
      case Down      => Position(x, y + 1)
      case DownLeft  => if (x % 2 == 0) Position(x - 1, y) else Position(x - 1, y + 1)
      case UpLeft    => if (x % 2 == 0) Position(x - 1, y - 1) else Position(x - 1, y)
    }
  }

  def turnLeft(dir: Direction): Direction = dir match {
    case Up        => UpLeft
    case UpRight   => Up
    case DownRight => UpRight
    case Down      => DownRight
    case DownLeft  => Down
    case UpLeft    => DownLeft
  }

  def turnRight(dir: Direction): Direction = dir match {
    case Up        => UpRight
    case UpRight   => DownRight
    case DownRight => Down
    case Down      => DownLeft
    case DownLeft  => UpLeft
    case UpLeft    => Up
  }

  def back(dir: Direction): Direction = turnRight(turnRight(turnRight(dir)))
}
