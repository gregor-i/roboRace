package controller

import api.User
import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.{Action, InjectedController}
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.libs.circe.Circe

@Singleton()
class UserController @Inject() (sessionAction: SessionAction) extends InjectedController with Circe {
  def me() = sessionAction { (session, _) =>
    Ok(User(sessionId = session.id).asJson)
  }
}
