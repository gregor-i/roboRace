package entities

sealed trait Direction

sealed trait WallDirection extends Direction

case object Up        extends Direction
case object UpRight   extends Direction with WallDirection
case object DownRight extends Direction with WallDirection
case object Down      extends Direction with WallDirection
case object DownLeft  extends Direction
case object UpLeft    extends Direction
