package roborace.frontend.preview

import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.error.ErrorState
import roborace.frontend.loading.LoadingFrontendState
import roborace.frontend.service.Service
import roborace.frontend.{FrontendState, Page, PreviewFrontendState, User}
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global

object PreviewPage extends Page[PreviewFrontendState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, s"/scenario/${scenarioId}/preview", _) =>
      LoadingFrontendState(
        for {
          scenarios <- Service.getAllScenarios()
          thisScenario = scenarios.find(_.id == scenarioId)
          state = thisScenario match {
            case None           => ErrorState("unknown Scenario")
            case Some(scenario) => PreviewFrontendState(scenario)
          }
        } yield state
      )
  }

  override def stateToUrl(state: State): Option[(Path, QueryParameter)] = Some(s"/scenario/${state.scenario.id}/preview" -> Map.empty)

  override def render(implicit state: State, update: FrontendState => Unit): Node = PreviewUi.render(state, update)
}
