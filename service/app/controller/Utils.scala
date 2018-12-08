package controller

import java.util.UUID

import play.api.mvc.Request
import play.utils.UriEncoding

object Utils {
  def newId(): String = UUID.randomUUID().toString
}
