package controller

import com.typesafe.config.ConfigValue
import io.circe.Encoder
import io.circe.syntax._
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController

class AdminController @Inject()(configuration: Configuration) extends InjectedController with Circe {
  implicit val encodeConfigVal: Encoder[ConfigValue] = Encoder.encodeString.contramap[ConfigValue](_.unwrapped.toString)

  def config() = Action {
    Ok(configuration.entrySet.toMap.asJson)
  }
}
