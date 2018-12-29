package frontend.gameBoard

import com.raquo.snabbdom.simple._
import com.raquo.snabbdom.simple.attrs.name
import com.raquo.snabbdom.simple.implicits._
import com.raquo.snabbdom.simple.props.className
import com.raquo.snabbdom.simple.tags.div
import frontend.util.{Dynamic, Hooks, Ui}
import gameEntities.{Direction, GameResponse, Position}
import org.scalajs.dom

object RenderGame extends Ui {
  def apply(game: GameResponse, click: Option[(Position, Direction) => Unit]): VNode = {
    var oldDuration, oldTime = 0d

    div(className := "game-board",
      Hooks { hooks =>
        hooks.addInsertHook { node =>
          val svg = dom.document.getElementsByTagName("svg").item(0)
          Dynamic(svg).dataset.duration = Animation.eventSequenceDuration(game.events)
        }
        hooks.addPrePatchHook { (oldNode, newNode) =>
          val oldSvg = Dynamic(oldNode.elm.get.firstChild)
          oldDuration = oldSvg.dataset.duration.asInstanceOf[String].toDouble
          oldTime = oldSvg.getCurrentTime().asInstanceOf[Double]
        }
        hooks.addPostPatchHook { (oldNode, newNode) =>
          val newSvg = Dynamic(newNode.elm.get.firstChild)
          val newDuration = Animation.eventSequenceDuration(game.events)
          if (oldDuration != newDuration) {
            newSvg.dataset.duration = newDuration
            newSvg.setCurrentTime(Math.min(oldTime, oldDuration))
          }
        }
      },
      tags.build("svg")(
        attrs.build[String]("viewBox") := s"0 0 ${Svg.width(game.scenario)} ${Svg.height(game.scenario)}",
        tags.build("g")(
          name := "tiles",
          seq(Svg.tiles(game.scenario, click))
        ),
        tags.build("g")(seq(Svg.walls(game.scenario))),
        tags.build("g")(Svg.target(game.scenario)),
        tags.build("g")(seq(Svg.traps(game.scenario))),
        tags.build("g")(seq(Svg.startPoints(game.scenario))),
        tags.build("g")(seq(Svg.robots(game.robots))),
        tags.build("g")(seq(Animation.animations(game))),
        NameSpace("http://www.w3.org/2000/svg"),
      )
    )
  }

}
