package controller

import java.util.UUID

import play.api.mvc.Request

object Utils {
  def playerName(request: Request[_]): Option[String] =
    request.cookies.get("playerName").map(_.value)


  def newShortId(): String = UUID.randomUUID().toString.take(8)
}
