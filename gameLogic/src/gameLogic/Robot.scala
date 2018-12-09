package gameLogic

import monocle.macros.Lenses

@Lenses
case class Robot(index: Int, position: Position, direction: Direction)
