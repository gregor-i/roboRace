package roborace.frontend.pages.components

import snabbdom.Node

object Header {
  def apply(): Node =
    Node("nav.navbar.is-light")
      .child(
        Node("div.navbar-brand")
          .child(
            Node("a")
              .classes("navbar-item")
              .attr("href", "/")
              .child(Node("img").attr("src", Images.logo))
          )
      )
      .child(
        Node("div.navbar-menu").child(
          Node("a.navbar-item")
            .attr("href", "#")
            .text("Tutorial")
        )
      )

}
