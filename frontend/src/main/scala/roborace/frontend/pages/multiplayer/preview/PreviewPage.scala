package roborace.frontend.pages
package multiplayer.preview

import api.WithId
import entities.Scenario
import roborace.frontend.service.Service
import roborace.frontend.{GlobalState, PageState}
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global

case class PreviewState(scenario: WithId[Scenario]) extends PageState

object PreviewPage extends Page[PreviewState] {
  override def stateFromUrl: PartialFunction[(GlobalState, Path, QueryParameter), PageState] = {
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

  override def render(implicit context: Context): Node = PreviewUi.render(context)
}
