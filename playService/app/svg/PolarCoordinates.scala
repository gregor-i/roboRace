package svg

case class PolarCoordinates(angle: Double, dist: Double) {
  val x = dist * Math.cos(angle / 180 * Math.PI)
  val y = dist * Math.sin(angle / 180 * Math.PI)

  def svg = s"$x $y"
}
