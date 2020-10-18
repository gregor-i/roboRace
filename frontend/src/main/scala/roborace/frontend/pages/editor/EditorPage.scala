package roborace.frontend.pages
package editor

import api.{Entity, WithId}
import entities.Scenario
import logic.DefaultScenario
import monocle.macros.Lenses
import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.service.Service
import roborace.frontend.{GlobalState, PageState}
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Lenses
case class EditorState(
    remoteScenario: Option[WithId[Entity[Scenario]]],
    scenario: Scenario,
    description: String = "",
    clickAction: Option[ClickAction] = None
) extends PageState {
  def dirty: Boolean = remoteScenario match {
    case Some(remoteScenario) => remoteScenario.entity.value != scenario || remoteScenario.entity.description != description
    case None                 => true
  }
}

object EditorState {
  def apply(remoteScenario: WithId[Entity[Scenario]]): EditorState =
    new EditorState(
      remoteScenario = Some(remoteScenario),
      scenario = remoteScenario.entity.value,
      description = remoteScenario.entity.description,
      clickAction = None
    )
}

object EditorPage extends Page[EditorState] {
  override def stateFromUrl: PartialFunction[(GlobalState, Path, QueryParameter), PageState] = {
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

  override def render(implicit context: Context): Node = EditorUi(context)
}
