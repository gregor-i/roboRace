package gameLogic.eventLog

case class Logged[+A, +L](state: A, events: Seq[L]) {
  def map[B](f: A => B): Logged[B, L] = Logged(f(state), events)

  def flatMap[B, LL >: L](f: A => Logged[B, LL]): Logged[B, LL] = {
    val r = f(state)
    Logged(r.state, events ++ r.events)
  }
}

object Logged {
  def pure[A](a: A): Logged[A, Nothing] = Logged(a, Nil)
}

trait LoggedSyntax {
  implicit class EnrichAny[A](val a: A) {
    def log[L](events: L*): Logged[A, L] = Logged(a, events)
  }
}

object LoggedSyntax extends LoggedSyntax

