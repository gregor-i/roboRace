package controller

import io.circe.syntax._
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController

import javax.inject.{Inject, Singleton}

@Singleton
class SessionController @Inject() (sessionAction: SessionAction) extends InjectedController with Circe {
  def get() = sessionAction { (session, _) =>
    Ok(session.id.asJson)
  }
}
