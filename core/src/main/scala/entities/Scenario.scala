package entities

import monocle.macros.Lenses

@Lenses
case class Scenario(
    width: Int,
    height: Int,
    targets: Seq[Position],
    initialRobots: Seq[Robot],
    walls: Seq[Wall],
    pits: Seq[Position],
    traps: Seq[Trap] = Seq.empty,
    description: String = ""
)
