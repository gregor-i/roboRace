package frontend.util

import scala.scalajs.js

object Dynamic {
  @inline
  def apply(any:js.Object): js.Dynamic = any.asInstanceOf[js.Dynamic]
}
