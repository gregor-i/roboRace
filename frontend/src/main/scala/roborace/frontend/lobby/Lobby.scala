//package roborace.frontend.lobby
//
//import com.raquo.snabbdom.simple.VNode
//import roborace.frontend.{LobbyState, Service}
//import roborace.frontend.util.SnabbdomApp
//import gameEntities.GameResponse
//import io.circe.generic.auto._
//import io.circe.parser._
//import org.scalajs.dom.raw.Element
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.scalajs.js.|
//
//class Lobby(container: Element) extends SnabbdomApp {
//
//  var node: Element | VNode = container
//
//  val eventSource = Service.lobbyUpdates()
//
//  def renderState(state: LobbyState): Unit = {
//    eventSource.onmessage = message => {
//      val newGames = decode[Seq[GameResponse]](message.data.asInstanceOf[String]).right.get
//      renderState(state.copy(games = newGames))
//    }
//    node = patch(node, LobbyUi.render(state))
//
//  }
//
//  for {
//    games <- frontend.Service.getAllGames()
//    scenarios <- frontend.Service.getAllScenarios()
//  } renderState(LobbyState(games, scenarios))
//}
