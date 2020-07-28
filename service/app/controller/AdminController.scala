package controller

import javax.inject.Inject
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController

class AdminController @Inject() () extends InjectedController with Circe {
  def ui() = Action {
    Ok(views.html.Admin())
  }
}
