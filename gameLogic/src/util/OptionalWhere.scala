package util

import monocle.Optional

trait OptionalWhere {
  def where[A](selector: A => Boolean) : Optional[List[A], A] = Optional[List[A], A](_.find(selector))(player => players => players.map(p => if(selector(p)) player else p))
}

object OptionalWhere extends OptionalWhere