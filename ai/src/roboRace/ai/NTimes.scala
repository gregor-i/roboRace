package roboRace.ai

import scala.annotation.tailrec

object NTimes{
  def apply[A](n: Int)(f0: A => A): A => A = {
    @tailrec
    def loop(n: Int, f: A => A): A => A =
      if(n == 0)
        f
      else
        loop(n-1, f0 compose f)
    loop(n, identity)
  }
}