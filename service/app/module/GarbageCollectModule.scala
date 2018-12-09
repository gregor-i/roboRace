package module

import java.time.ZonedDateTime

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.google.inject.AbstractModule
import javax.inject.{Inject, Singleton}
import play.api.Logger
import repo._

import scala.concurrent.duration._

class GarbageCollectModule extends AbstractModule{
  override def configure(): Unit = {
    bind(classOf[GarbageCollectorThread])
      .asEagerSingleton()
  }
}

@Singleton
class GarbageCollectorThread @Inject()(gameRepository: GameRepository,
                                       scenarioRepository: ScenarioRepository,
                                       sessionRepo: SessionRepo)
                                      (implicit mat: Materializer){

  val tickInterval: FiniteDuration = 1.minute
  val sessionInactivityTime: FiniteDuration = 60.minutes
  val sessionLifetime: FiniteDuration = 1.day

  def gameDeletePredicate(activePlayers: Seq[String])(gameRow: GameRow) : Boolean =
    gameRow.game.isEmpty ||
    !gameRow.game.get.players.map(_.name).exists(activePlayers.contains)

  def scenarioDeletePredicate(scenarioRow: ScenarioRow): Boolean =
    scenarioRow.scenario.isEmpty

  def sessionDeletePredicate(session: Session): Boolean =
    session.lastActivityAt.plusSeconds(sessionInactivityTime.toSeconds).isBefore(ZonedDateTime.now()) ||
    session.startedAt.plusSeconds(sessionLifetime.toSeconds).isBefore(ZonedDateTime.now())

  Source.tick(0.seconds, tickInterval, ())
    .to(Sink.foreach{_ =>

      val deletedSessions = sessionRepo.list()
          .filter(sessionDeletePredicate)
          .map(_.id)
          .map(sessionRepo.delete)
          .size

      val activePlayers = sessionRepo.list().map(_.playerId)
      val deletedGames = gameRepository.list()
        .filter(gameDeletePredicate(activePlayers))
        .map(_.id)
        .map(gameRepository.delete)
        .size

      val deletedScenarios = scenarioRepository.list()
        .filter(scenarioDeletePredicate)
        .map(_.id)
        .map(scenarioRepository.delete)
        .size

      Logger.info("GarbageCollectorThread ticked." +
        s"Deleted $deletedSessions sessions, $deletedGames games, $deletedScenarios scenarios")
    })
    .run()
}
