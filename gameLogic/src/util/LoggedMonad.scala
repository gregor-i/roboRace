package util

import scala.collection.GenTraversableOnce

case class LoggedMonad[+A, +L](state: A, events: Seq[L]) {
  def map[B](f: A => B): LoggedMonad[B, L] = LoggedMonad(f(state), events)

  def flatMap[B, LL >: L](f: A => LoggedMonad[B, LL]): LoggedMonad[B, LL] = {
    val r = f(state)
    LoggedMonad(r.state, events ++ r.events)
  }
}

object LoggedMonad {
  def pure[A](a: A): LoggedMonad[A, Nothing] = LoggedMonad(a, Nil)

  def flatMapFold[A, B](init: LoggedMonad[A, B])(ops: GenTraversableOnce[A => LoggedMonad[A, B]]): LoggedMonad[A, B] =
    ops.foldLeft(init)((state, operation) => state.flatMap(operation))
}

trait LoggedMonadSyntax {
  implicit class EnrichAny[A](private val a: A) {
    def log[L](events: L*): LoggedMonad[A, L] = LoggedMonad(a, events)
  }
}

object LoggedMonadSyntax extends LoggedMonadSyntax

