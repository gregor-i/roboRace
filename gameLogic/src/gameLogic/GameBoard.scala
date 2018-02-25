package gameLogic

case class Position(x: Int, y: Int){
  def move(dir: Direction):Position = dir match {
    case Up => Position(x, y-1)
    case Down => Position(x, y+1)
    case Right => Position(x+1, y)
    case Left => Position(x-1, y)
  }
}

sealed trait Direction{
  def left: Direction
  def right: Direction
  def back = left.left
}

sealed trait WallDirection

case object Up extends Direction{
  def left = Left
  def right = Right
}
case object Down extends Direction with WallDirection {
  def left = Right
  def right = Left
}
case object Right extends Direction with WallDirection {
  def left = Up
  def right = Down
}
case object Left extends Direction {
  def left = Down
  def right = Up
}
