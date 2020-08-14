package logic

import entities.Game

trait State {
  def all(fs: Seq[Game => Game]): Game => Game =
    game => fs.foldLeft(game)((s, f) => f(s))

  def sequence(fs: (Game => Game)*): Game => Game =
    all(fs)

  def conditional(cond: Game => Boolean)(f: Game => Game): Game => Game =
    game => if (cond(game)) f(game) else game
}

object State extends State
