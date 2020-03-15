//package roborace.frontend.gameBoard
//
//import com.raquo.snabbdom.simple._
//import com.raquo.snabbdom.simple.attrs.name
//import com.raquo.snabbdom.simple.implicits._
//import com.raquo.snabbdom.simple.props.className
//import com.raquo.snabbdom.simple.tags.div
//import roborace.frontend.util._
//import gameEntities._
//
//
//object RenderScenario extends Ui {
//  def apply(scenario: Scenario, click: Option[(Position, Direction) => Unit]): VNode = {
//    div(className := "game-board",
//      tags.build("svg")(
//        attrs.build[String]("viewBox") := s"0 0 ${Svg.width(scenario)} ${Svg.height(scenario)}",
//        tags.build("g")(
//          name := "tiles",
//          seq(Svg.tiles(scenario, click))
//        ),
//        tags.build("g")(seq(Svg.walls(scenario))),
//        tags.build("g")(seq(Svg.targets(scenario, None))),
//        tags.build("g")(seq(Svg.traps(scenario))),
//        tags.build("g")(seq(Svg.startPoints(scenario))),
//        NameSpace("http://www.w3.org/2000/svg"),
//      )
//    )
//  }
//}
