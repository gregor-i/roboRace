package roborace.frontend.game

object Game {
  def clearSlots(oldState: GameState, newState: GameState): GameState =
    if (oldState.game.cycle != newState.game.cycle) {
      oldState.copy(focusedSlot = 0, slots = Map.empty, game = newState.game)
    } else {
      oldState.copy(game = newState.game)
    }
}
