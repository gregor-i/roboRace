package controller

import play.api.Logger
import play.api.mvc.Request

object Utils {
  def playerName(request: Request[_]): Option[String] = {
    Logger.info(s"cookies: ${request.cookies}")
    request.cookies.get("playerName").map(_.value)
  }

  def fallbackPlayerName = "observer"
}
