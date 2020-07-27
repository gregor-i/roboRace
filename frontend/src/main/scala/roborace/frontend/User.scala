package roborace.frontend

// todo: should be more general then frontend
case class User(id: String, name: String, email: String, googleUserId: Option[String], admin: Boolean = false)
