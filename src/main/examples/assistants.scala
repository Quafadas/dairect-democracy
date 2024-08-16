package io.github.quafadas.dairect

import cats.effect.IOApp
import cats.effect.IO
import scala.annotation.experimental
import smithy4s.Document

@experimental
object Assistant extends IOApp.Simple:

  def run: IO[Unit] =
    val a = AssistantApi.defaultAuthLogToFile(fs2.io.file.Path("assistant.txt"))
    a.use { assistant =>
      // assistant
      //   .create("gpt-4o-mini")
      //   .flatMap(IO.println) >>
      assistant.assistants().flatMap(IO.println) >>
        assistant.getAssisstant("asst_7g7FJuGyXC8mXGXUg0fTMuOa").flatMap(IO.println)
    }

  end run

end Assistant
