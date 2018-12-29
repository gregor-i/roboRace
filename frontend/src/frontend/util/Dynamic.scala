package frontend.util

import scala.scalajs.js

object Dynamic {
  def apply(any:js.Object): js.Dynamic = any.asInstanceOf[js.Dynamic]
}
