package frontend.lobby

import gameEntities.{GameResponse, ScenarioResponse}

case class LobbyState(games: Seq[GameResponse],
                      scenarios: Seq[ScenarioResponse])