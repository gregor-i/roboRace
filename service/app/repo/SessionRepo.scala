package repo

import java.time.ZonedDateTime

import javax.inject.Singleton

case class Session(id: String, playerId: String, startedAt: ZonedDateTime, lastActivityAt: ZonedDateTime)

@Singleton
class SessionRepo {
  private var cache: Map[String, Session] = Map.empty

  def get(id: String): Option[Session] = cache.get(id)
  def list(): Seq[Session]             = cache.values.toSeq
  def delete(id: String): Unit         = cache -= id
  def set(session: Session): Unit      = cache += (session.id -> session)
}
