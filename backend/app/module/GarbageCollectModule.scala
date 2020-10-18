package module

import java.time.ZonedDateTime

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import api.{Entity, WithId}
import com.google.inject.AbstractModule
import entities.Scenario
import javax.inject.{Inject, Singleton}
import play.api.Logger
import repo._

import scala.concurrent.duration._

class GarbageCollectModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[GarbageCollectorThread])
      .asEagerSingleton()
  }
}

@Singleton
class GarbageCollectorThread @Inject() (gameRepository: GameRepository, scenarioRepository: ScenarioRepository, sessionRepo: SessionRepo)(
    implicit mat: Materializer
) {

  private val logger = Logger(this.getClass)

  val tickInterval: FiniteDuration          = 10.minute
  val sessionInactivityTime: FiniteDuration = 1.day

  def gameDeletePredicate(activePlayers: Seq[String])(gameRow: GameRow): Boolean =
    gameRow.game.isEmpty ||
      !gameRow.game.get.players.map(_.id).exists(activePlayers.contains)

  def scenarioDeletePredicate(scenarioRow: WithId[Option[Entity[Scenario]]]): Boolean =
    scenarioRow.entity.isEmpty

  def sessionDeletePredicate(session: Session): Boolean =
    session.lastActivityAt.plusSeconds(sessionInactivityTime.toSeconds).isBefore(ZonedDateTime.now())

  Source
    .tick(0.seconds, tickInterval, ())
    .to(Sink.foreach { _ =>
      val deletedSessions = sessionRepo
        .list()
        .filter(sessionDeletePredicate)
        .map(_.id)
        .map(sessionRepo.delete)
        .size

      val activePlayers = sessionRepo.list().map(_.id)
      val deletedGames = gameRepository
        .list()
        .filter(gameDeletePredicate(activePlayers))
        .map(_.id)
        .map(gameRepository.delete)
        .size

      val deletedScenarios = scenarioRepository
        .list()
        .filter(scenarioDeletePredicate)
        .map(_.id)
        .map(scenarioRepository.delete)
        .size

      if (deletedGames + deletedScenarios + deletedSessions != 0)
        logger.info(
          "GarbageCollectorThread ticked. " +
            s"Deleted $deletedSessions sessions, $deletedGames games, $deletedScenarios scenarios"
        )
    })
    .run()
}
