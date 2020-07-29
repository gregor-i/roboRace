package roborace.frontend.service

import io.circe.syntax._
import io.circe.{Decoder, Encoder, parser}
import org.scalajs.dom.experimental.{Fetch, RequestInit, Response}

import scala.concurrent.Future
import scala.scalajs.js.Dynamic

import scala.concurrent.ExecutionContext.Implicits.global

private[service] trait ServiceTrait {

  private[service] def get(url: String): Future[Response] = Fetch.fetch(url).toFuture

  private[service] def post(url: String): Future[Response] =
    Fetch.fetch(url, Dynamic.literal(method = "POST").asInstanceOf[RequestInit]).toFuture

  private[service] def delete(url: String): Future[Response] =
    Fetch.fetch(url, Dynamic.literal(method = "DELETE").asInstanceOf[RequestInit]).toFuture

  private[service] def post[A: Encoder](url: String, body: A): Future[Response] =
    Fetch.fetch(url, Dynamic.literal(method = "POST", body = body.asJson.noSpaces).asInstanceOf[RequestInit]).toFuture

  private[service] def put[A: Encoder](url: String, body: A): Future[Response] =
    Fetch.fetch(url, Dynamic.literal(method = "PUT", body = body.asJson.noSpaces).asInstanceOf[RequestInit]).toFuture

  private[service] def check(excepted: Int)(req: Response): Future[Response] =
    if (req.status == excepted)
      Future.successful(req)
    else
      Future.failed(new Exception(s"unexpected response code: ${req.status}"))

  private[service] def parse[A: Decoder](req: Response): Future[A] =
    req
      .text()
      .toFuture
      .map(parser.decode[A])
      .map(_.toTry)
      .flatMap(Future.fromTry)

  private[service] def toUnit(fut: Future[_]): Future[Unit] = fut.map(_ => ())
}

object ServiceTrait extends ServiceTrait
