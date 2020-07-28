package roborace.frontend

import org.scalajs.dom

import scala.scalajs.js

object Main {
  def main(args: Array[String]): Unit = {
    dom.document.addEventListener[dom.Event](
      "DOMContentLoaded",
      (_: js.Any) => {
        val container = dom.document.getElementById("robo-race")
        new RoboRaceApp(container)
      }
    )
  }
}
