package api

import entities.Scenario

case class ScenarioResponse(id: String, description: String, scenario: Scenario, ownedByYou: Boolean)
