package controller

import gameLogic._
import gameLogic.command.{CommandAccepted, CreateGame}
import gameLogic.gameUpdate.Cycle
import javax.inject.{Inject, Singleton}
import play.api.mvc.InjectedController
import repo.NeuronalNetworkRepository
import roboRace.ai.{Bot, Learn, NeuronalNetwork, NeuronalNetworkGenes}

@Singleton()
class AiController @Inject()(repo: NeuronalNetworkRepository) extends InjectedController {
  def createGame(scenario: Scenario)(player: String): Game =
    CreateGame(scenario)(player).asInstanceOf[CommandAccepted].newState

  def sequenceWithAutoCycle(state: Game)(fs: (Game => Game)*): Game =
    State.sequence(fs.map(_ andThen Cycle): _*)(state)

  def botInstructions(bot: Bot, player: String): Game => Game = game => {
    val p = game.players.find(_.name == player).get
    val chosen = bot.apply(game.scenario, game.players.filter(_.name != player).map(_.robot))(p.robot, p.instructionOptions)
    Game.player(player).composeLens(Player.instructionSlots).set(chosen.map(Some.apply))(game)
  }

  def botFinishesGame(bot: Bot, maxCycles: Int, scenario: Scenario): Boolean = {
    val g = sequenceWithAutoCycle(createGame(scenario)("bot"))(
      Seq.fill(maxCycles)(botInstructions(bot, "bot")): _*
    )
    g.players.head.finished.isDefined
  }

  def filtering(scenario: Scenario, iterations: Int = 1): NeuronalNetworkGenes => Boolean = genes => botFinishesGame(NeuronalNetwork(genes), iterations, scenario)

  val scenario1 = Scenario(1, 3, Position(0, 0), Seq(Robot(Position(0, 2), Up)), Seq.empty, Seq.empty, Seq.empty)
  val scenario2 = Scenario(2,2, Position(0, 0), Seq(Robot(Position(1, 1), Up)), Seq.empty, Seq.empty, Seq.empty)
  val scenario3 = Scenario(2,1, Position(1, 0), Seq(Robot(Position(0, 0), Up)), Seq.empty, Seq.empty, Seq.empty)

  val filter1 = filtering(scenario1)
  val filter2 = filtering(scenario2)
  val filter3 = filtering(scenario3)

  val scenarios: Map[String, NeuronalNetworkGenes => Boolean] = Map("1" -> filter1, "2" -> filter2, "3" -> filter3)

  def learn(scenarioId: String) = Action(
    scenarios.get(scenarioId) match {
      case Some(filter) =>
        val state = repo.read().getOrElse(Seq.tabulate(100)(i => NeuronalNetwork.genesFromSeed(i.toLong)))
        val newState = Learn.learn[NeuronalNetworkGenes](filter, NeuronalNetwork.breed(_, 100, 1L))(state)
        repo.save(newState)
        Ok(state.size.toString)
      case None =>
        NotFound
    }
  )
}
