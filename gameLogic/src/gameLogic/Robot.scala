package gameLogic

import monocle.macros.Lenses

@Lenses
case class Robot(position: Position, direction: Direction)
