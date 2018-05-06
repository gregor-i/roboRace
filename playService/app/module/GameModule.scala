package module

import com.google.inject.{AbstractModule, Provides}
import javax.inject.Singleton
import play.api.{Environment, Mode}
import repo.{FileGameRepository, GameRepository, MemoryGameRepository}

class GameModule extends AbstractModule {
  override def configure(): Unit = ()

  @Provides
  @Singleton
  def provideRepo(app: Environment): GameRepository = {
    if (app.mode == Mode.Dev) new FileGameRepository()
    else new MemoryGameRepository()
  }
}
