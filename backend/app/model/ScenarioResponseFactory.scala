package model

import api.ScenarioResponse
import repo.{ScenarioRow, Session}

object ScenarioResponseFactory {
  def apply(scenarioRow: ScenarioRow)(implicit session: Session): Option[ScenarioResponse] =
    scenarioRow.scenario.map(
      scenario =>
        api.ScenarioResponse(
          id = scenarioRow.id,
          description = scenarioRow.description,
          scenario = scenario,
          ownedByYou = scenarioRow.owner == session.playerId
        )
    )
}
