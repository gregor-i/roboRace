package roborace.frontend

import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.scalajs.dom
import roborace.frontend.error.ErrorState

import scala.util.Try
import scala.util.chaining._

object Router {
  type Path           = String
  type QueryParameter = Map[String, String]
  type Location       = (Path, QueryParameter)

  def stateFromUrl(location: dom.Location, user: Option[User]): FrontendState =
    stateFromUrl((location.pathname, queryParamsFromUrl(location.search)): Location, user)

  private val stateFromUrlPF: ((Option[User], Path, QueryParameter)) => Option[FrontendState] =
    Pages.all
      .map(_.stateFromUrl)
      .reduce(_ orElse _)
      .lift

  def stateFromUrl(location: Location, user: Option[User]): FrontendState =
    stateFromUrlPF((user, location._1, location._2)).getOrElse(ErrorState("unkown url"))

  def stateToUrl(state: FrontendState): Option[Location] =
    Pages.selectPage(state).stateToUrl(state)

  def queryParamsToUrl(search: QueryParameter): String = {
    val stringSearch = search
      .map {
        case (key, value) => s"$key=$value"
      }
      .mkString("&")
    if (stringSearch == "")
      ""
    else
      "?" + stringSearch
  }

  def queryParamsFromUrl(search: String): QueryParameter =
    search
      .dropWhile(_ == '?')
      .split('&')
      .collect {
        case s"${key}=${value}" => key -> value
      }
      .toMap

  def queryEncoded[T: Encoder](t: T): String =
    t.asJson.noSpaces
      .pipe(dom.window.btoa)

  def queryDecoded[T: Decoder](string: String): Option[T] =
    (for {
      decoded <- Try(dom.window.atob(string)).toEither
      json    <- io.circe.parser.parse(new String(decoded))
      decoded <- json.as[T]
    } yield decoded).toOption
}
