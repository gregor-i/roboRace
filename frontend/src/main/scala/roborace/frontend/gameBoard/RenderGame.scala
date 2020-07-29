package roborace.frontend.gameBoard

import gameEntities.{Direction, GameResponse, Position, RunningPlayer}
import org.scalajs.dom.raw.HTMLElement
import roborace.frontend.util.Untyped
import snabbdom.{Node, Snabbdom}

object RenderGame {
  def apply(game: GameResponse, click: Option[(Position, Direction) => Unit]): Node = {
    var oldDuration, oldTime = 0d

    Node("div.game-board")
      .hook(
        "insert",
        Snabbdom.hook { node =>
          val svg = node.elm.get.getElementsByTagName("svg").item(0).asInstanceOf[HTMLElement]
          svg.dataset.update("duration", Animation.eventSequenceDuration(game.events).toString)
        }
      )
      .hook(
        "prepatch",
        Snabbdom.hook { (oldNode, newNode) =>
          val oldSvg = oldNode.elm.get.firstChild.asInstanceOf[HTMLElement]
          oldDuration = oldSvg.dataset.get("duration").get.toDouble
          oldTime = Untyped(oldSvg).getCurrentTime().asInstanceOf[Double]
        }
      )
      .hook(
        "postpatch",
        Snabbdom.hook { (oldNode, newNode) =>
          val newSvg      = newNode.elm.get.firstChild.asInstanceOf[HTMLElement]
          val newDuration = Animation.eventSequenceDuration(game.events)
          if (oldDuration != newDuration) {
            newSvg.dataset.update("duration", newDuration.toString)
            Untyped(newSvg).setCurrentTime(Math.min(oldTime, oldDuration))
          }
        }
      )
      .child(
        Node("svg")
          .attr("viewBox", s"0 0 ${Svg.width(game.scenario)} ${Svg.height(game.scenario)}")
          .children(
            group("names", Svg.tiles(game.scenario, click)),
            group("walls", Svg.walls(game.scenario)),
            group("targets", Svg.targets(game.scenario, game.you.collect { case p: RunningPlayer => p.currentTarget })),
            group("traps", Svg.traps(game.scenario)),
            group("startPoints", Svg.startPoints(game.scenario)),
            group("robots", Svg.robots(game.scenario.initialRobots)),
            group("animations", Animation.animations(game))
          )
      )
  }
  private def group(name: String, nodes: Seq[Node]): Node =
    Node("g").attr("name", name).child(nodes)
}
