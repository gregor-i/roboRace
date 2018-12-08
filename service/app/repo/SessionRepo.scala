package repo

import java.time.ZonedDateTime

import javax.inject.Singleton

@Singleton
class SessionRepo {
  private var cache: Map[String, Session] = Map.empty

  def get(id: String): Option[Session] = cache.get(id)
  def delete(id: String): Unit = cache -= id
  def set(session: Session): Unit = cache += (session.id -> session)
}

case class Session(id: String, startedAt: ZonedDateTime, lastActivityAt: ZonedDateTime)
