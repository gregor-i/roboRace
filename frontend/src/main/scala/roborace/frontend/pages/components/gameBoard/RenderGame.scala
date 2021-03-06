package roborace.frontend.pages.components.gameBoard

import api.WithId
import entities._
import org.scalajs.dom.raw.HTMLElement
import roborace.frontend.Context
import roborace.frontend.util.Untyped
import snabbdom.{Node, Snabbdom}

object RenderGame {
  def apply(game: Game, click: Option[(Position, Direction) => Unit]): Node =
    // fixme: the None is wrong
    apply(game.scenario, game.events, None, click)

  def apply(game: WithId[Game], click: Option[(Position, Direction) => Unit])(implicit context: Context[_]): Node =
    apply(
      game.entity.scenario,
      game.entity.events,
      game.entity.players.filter(_.id == context.global.sessionId).collectFirst { case p: RunningPlayer => p.currentTarget },
      click
    )

  private def apply(scenario: Scenario, events: Seq[EventLog], activeTarget: Option[Int], click: Option[(Position, Direction) => Unit]): Node = {
    var oldDuration, oldTime = 0d

    Node("div.game-board")
      .hookInsert { node =>
        val svg = node.elm.get.getElementsByTagName("svg").item(0).asInstanceOf[HTMLElement]
        svg.dataset.update("duration", Animation.eventSequenceDuration(events).toString)
      }
      .hookPrepatch { (oldNode, newNode) =>
        val oldSvg = oldNode.elm.get.firstChild.asInstanceOf[HTMLElement]
        oldDuration = oldSvg.dataset.get("duration").fold(0.0)(_.toDouble)
        oldTime = Untyped(oldSvg).getCurrentTime().asInstanceOf[Double]
      }
      .hookPostpatch { (oldNode, newNode) =>
        val newSvg      = newNode.elm.get.firstChild.asInstanceOf[HTMLElement]
        val newDuration = Animation.eventSequenceDuration(events)
        if (oldDuration != newDuration) {
          newSvg.dataset.update("duration", newDuration.toString)
          Untyped(newSvg).setCurrentTime(Math.min(oldTime, oldDuration))
        }
      }
      .child(
        Node("svg")
          .attr("viewBox", s"0 0 ${Svg.width(scenario)} ${Svg.height(scenario)}")
          .children(
            group("tiles", Svg.tiles(scenario, click)),
            group("walls", Svg.walls(scenario)),
            group("targets", Svg.targets(scenario, activeTarget)),
            group("traps", Svg.traps(scenario)),
            group("startPoints", Svg.startPoints(scenario)),
            group("robots", Svg.robots(scenario.initialRobots)),
            group("animations", Animation.animations(events))
          )
      )
  }
  private def group(name: String, nodes: Seq[Node]): Node =
    Node("g").attr("name", name).child(nodes)
}
