package gameEntities

case class ScenarioResponse(id: String,
                            description: String,
                            scenario: Scenario,
                            ownedByYou: Boolean)
