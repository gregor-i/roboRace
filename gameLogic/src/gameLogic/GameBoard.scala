package gameLogic

case class Position(x: Int, y: Int) {
  override def toString() = s"($x, $y)"
}

sealed trait Direction {
  def apply(pos: Position): Position = {
    import pos.{x, y}
    this match {
      case Up => Position(x, y - 1)
      case UpRight => if (x % 2 == 0) Position(x + 1, y - 1) else Position(x + 1, y)
      case DownRight => if (x % 2 == 0) Position(x + 1, y) else Position(x + 1, y + 1)
      case Down => Position(x, y + 1)
      case DownLeft => if (x % 2 == 0) Position(x - 1, y) else Position(x - 1, y + 1)
      case UpLeft => if (x % 2 == 0) Position(x - 1, y - 1) else Position(x - 1, y)
    }
  }

  def left: Direction = this match {
    case Up => UpLeft
    case UpRight => Up
    case DownRight => UpRight
    case Down => DownRight
    case DownLeft => Down
    case UpLeft => DownLeft
  }

  def right: Direction = this match {
    case Up => UpRight
    case UpRight => DownRight
    case DownRight => Down
    case Down => DownLeft
    case DownLeft => UpLeft
    case UpLeft => Up
  }

  def back = left.left.left
}

sealed trait WallDirection

case object Up extends Direction
case object UpRight extends Direction with WallDirection
case object DownRight extends Direction with WallDirection
case object Down extends Direction with WallDirection
case object DownLeft extends Direction
case object UpLeft extends Direction
