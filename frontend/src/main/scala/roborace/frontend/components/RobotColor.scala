package roborace.frontend.components

object RobotColor {
  val robotLight1 = "#11b5e5"
  val robotLight2 = "#00e484"
  val robotLight3 = "#f75f5f"
  val robotLight4 = "#f5e64f"
  val robotLight5 = "#f59c4e"
  val robotLight6 = "#cd7bf3"

  val robotDark1 = "#85aad9"
  val robotDark2 = "#9aca6f"
  val robotDark3 = "#e4797f"
  val robotDark4 = "#ffc276"
  val robotDark5 = "#ed987a"
  val robotDark6 = "#d983c7"

  def light(index: Int): String = index match {
    case 0 => robotLight1
    case 1 => robotLight2
    case 2 => robotLight3
    case 3 => robotLight4
    case 4 => robotLight5
    case 5 => robotLight6
  }

  def dark(index: Int): String = index match {
    case 0 => robotDark1
    case 1 => robotDark2
    case 2 => robotDark3
    case 3 => robotDark4
    case 4 => robotDark5
    case 5 => robotDark6
  }
}
