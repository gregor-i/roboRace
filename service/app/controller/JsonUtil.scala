package controller

import java.time.ZonedDateTime

import io.circe.{Decoder, Encoder}

import scala.util.Try

trait JsonUtil {
  implicit val encodeZonedDateTime: Encoder[ZonedDateTime] =
    implicitly[Encoder[String]].contramap(d => d.toString)

  implicit val decodeZonedDateTime: Decoder[ZonedDateTime] =
    implicitly[Decoder[String]].emapTry(s => Try(ZonedDateTime.parse(s)))
}
