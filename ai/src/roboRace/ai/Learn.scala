package roboRace.ai

object Learn {
  def learn[G](predicate: G => Boolean, breed: Seq[G] => Seq[G]): Seq[G] => Seq[G] =
    genes => breed(genes).filter(predicate)
}


