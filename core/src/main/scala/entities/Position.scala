package entities

case class Position(x: Int, y: Int) {
  override def toString() = s"($x, $y)"
}
