package controller

import java.util.UUID

import play.api.mvc.Request
import play.utils.UriEncoding

object Utils {
  def playerName(request: Request[_]): Option[String] =
    request.cookies.get("playerName").map(_.value).map(UriEncoding.decodePath(_, "UTF-8"))


  def newShortId(): String = UUID.randomUUID().toString.take(8)
}
