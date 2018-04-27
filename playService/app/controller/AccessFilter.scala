package controller

import javax.inject.Inject

import akka.stream.Materializer
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}


class AccessFilter @Inject()()(implicit val mat: Materializer, ex: ExecutionContext) extends Filter {
  val Logger = play.api.Logger("access")

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    f(rh).map { resp =>
      Logger.info(s"${rh.method} to ${rh.path} returned ${resp.header.status}")
      resp
    }
  }
}
