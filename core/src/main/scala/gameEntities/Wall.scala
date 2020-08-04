package gameEntities

import gameLogic.Direction

case class Wall(position: Position, direction: WallDirection)

object Wall {
  def apply(position: Position, wallDirection: WallDirection): Wall =
    new Wall(position, wallDirection)

  def apply(position: Position, direction: Direction): Wall = direction match {
    case w: WallDirection => new Wall(position, w)
    case Up               => new Wall(Direction.move(direction, position), Down)
    case DownLeft         => new Wall(Direction.move(direction, position), UpRight)
    case UpLeft           => new Wall(Direction.move(direction, position), DownRight)
  }
}
