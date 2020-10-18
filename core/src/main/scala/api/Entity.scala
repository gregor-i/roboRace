package api

import io.circe.syntax._
import io.circe.{Codec, Decoder, Encoder}

@monocle.macros.Lenses()
case class Entity[A](
    title: String = "<no title>",
    description: String = "",
    value: A
) {
  def map[B](f: A => B): Entity[B] =
    copy(value = f(value))
}

object Entity {
  implicit def codec[A: Decoder: Encoder]: Codec[Entity[A]] = Codec.from(
    decodeA = Decoder[Entity[A]] { json =>
      for {
        value       <- json.as[A]
        title       <- json.downField("title").as[String]
        description <- json.downField("description").as[String]
      } yield Entity(title, description, value)
    },
    encodeA = Encoder[Entity[A]] { row =>
      Encoder[A]
        .apply(row.value)
        .mapObject(_.add("title", row.title.asJson))
        .mapObject(_.add("description", row.description.asJson))
    }
  )

  implicit def ordering[A: Ordering]: Ordering[Entity[A]] = Ordering.by(_.value)
}
