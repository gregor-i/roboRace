package api

import io.circe.generic.semiauto
import io.circe.{Codec, Decoder, Encoder}

@monocle.macros.Lenses()
case class WithId[A](id: String, owner: String, entity: A)

object WithId {
  implicit def codec[A: Encoder: Decoder]: Codec[WithId[A]] = semiauto.deriveCodec[WithId[A]]
}
