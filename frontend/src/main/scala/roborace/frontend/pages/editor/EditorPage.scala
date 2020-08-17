package roborace.frontend.pages
package editor

import api.{ScenarioResponse, User}
import entities.Scenario
import logic.DefaultScenario
import monocle.macros.Lenses
import roborace.frontend.FrontendState
import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.service.Service
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Lenses
case class EditorState(
    remoteScenario: Option[ScenarioResponse],
    scenario: Scenario,
    description: String = "",
    clickAction: Option[ClickAction] = None
) extends FrontendState {
  def dirty: Boolean = remoteScenario match {
    case Some(remoteScenario) => remoteScenario.scenario != scenario || remoteScenario.description != description
    case None                 => true
  }
}

object EditorState {
  def apply(remoteScenario: ScenarioResponse): EditorState =
    new EditorState(
      remoteScenario = Some(remoteScenario),
      scenario = remoteScenario.scenario,
      description = remoteScenario.description,
      clickAction = None
    )
}

object EditorPage extends Page[EditorState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, s"/editor/${scenarioId}", _) =>
      LoadingState {
        Service
          .getScenario(scenarioId)
          .transform {
            case Success(scenarioResponse) => Success(EditorState(scenarioResponse))
            case Failure(_)                => Success(ErrorState("scenario not found"))
          }
      }

    case (_, "/editor", _) =>
      EditorState(None, DefaultScenario.default, "default")
  }

  override def stateToUrl(state: EditorState): Option[(Path, QueryParameter)] =
    state.remoteScenario match {
      case Some(remoteScenario) => Some(s"/editor/${remoteScenario.id}" -> Map.empty)
      case None                 => Some("/editor"                       -> Map.empty)
    }

  override def render(implicit state: EditorState, update: FrontendState => Unit): Node =
    EditorUi(state, update)
}
