package roborace.frontend.pages
package preview

import api.User
import gameEntities.ScenarioResponse
import roborace.frontend.FrontendState
import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.service.Service
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global

case class PreviewState(scenario: ScenarioResponse) extends FrontendState

object PreviewPage extends Page[PreviewState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, s"/scenario/${scenarioId}/preview", _) =>
      LoadingState(
        for {
          scenarios <- Service.getAllScenarios()
          thisScenario = scenarios.find(_.id == scenarioId)
          state = thisScenario match {
            case None           => ErrorState("unknown Scenario")
            case Some(scenario) => PreviewState(scenario)
          }
        } yield state
      )
  }

  override def stateToUrl(state: State): Option[(Path, QueryParameter)] = Some(s"/scenario/${state.scenario.id}/preview" -> Map.empty)

  override def render(implicit state: State, update: FrontendState => Unit): Node = PreviewUi.render(state, update)
}
