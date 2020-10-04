package roborace.frontend

import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.scalajs.dom
import roborace.frontend.pages.ErrorState

import scala.util.Try
import scala.util.chaining._

object Router {
  type Path           = String
  type QueryParameter = Map[String, String]
  type Location       = (Path, QueryParameter)

  def stateFromUrl(globalState: GlobalState, location: dom.Location): PageState =
    stateFromUrl(globalState, (location.pathname, queryParamsFromUrl(location.search)): Location)

  private val stateFromUrlPF: ((GlobalState, Path, QueryParameter)) => Option[PageState] =
    Pages.all
      .map(_.stateFromUrl)
      .reduce(_ orElse _)
      .lift

  def stateFromUrl(globalState: GlobalState, location: Location): PageState =
    stateFromUrlPF((globalState, location._1, location._2)).getOrElse(ErrorState("unknown url"))

  def stateToUrl(state: PageState): Option[Location] =
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
