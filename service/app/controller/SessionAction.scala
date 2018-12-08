package controller

import java.time.ZonedDateTime
import java.util.UUID

import javax.inject.Inject
import play.api.mvc._
import repo.{Session, SessionRepo}

class SessionAction @Inject()(sessionRepo: SessionRepo) {

  val sessionCookieName = "sessionId"

  def createNewSession(): Session = Session(
    id = Utils.newId(),
    startedAt = ZonedDateTime.now(),
    lastActivityAt = ZonedDateTime.now()
  )

  private def getSession(req: Request[_]): Session = {
    val maybeSession = for {
      sessionCookie <- req.cookies.find(_.name == sessionCookieName)
      sessionId = sessionCookie.value
      session <- sessionRepo.get(sessionId)
    } yield session

    maybeSession.getOrElse(createNewSession())
  }

  private def updateRepo(session: Session): Unit =
    sessionRepo.set(session.copy(lastActivityAt = ZonedDateTime.now()))

  private def updateResponse(resp: Result, session: Session): Result = {
    val sessionCookie = Cookie(name = sessionCookieName, value = session.id, httpOnly = true)
    resp.withCookies(sessionCookie)
  }

  def apply(f: (Session, Request[AnyContent]) => Result): Action[AnyContent] =
    Action { request =>
      val session = getSession(request)
      updateRepo(session)
      updateResponse(f(session, request), session)
    }

  def apply[A](parser: BodyParser[A])(f: (Session, Request[A]) => Result): Action[A] =
    Action[A](parser) { request =>
      val session = getSession(request)
      updateRepo(session)
      updateResponse(f(session, request), session)
    }

}
