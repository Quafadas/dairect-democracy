Call the assistant API

```scala mdoc:passthrough

import io.github.quafadas.dairect.*
import cats.effect.IOApp
import cats.effect.IO
import scala.annotation.experimental
import smithy4s._
import smithy4s.kinds.PolyFunction
import cats.effect.unsafe.implicits.global

object Assistant extends IOApp.Simple:

  def run: IO[Unit] =
    val a = AssistantApi.defaultAuthLogToFileAddHeader(fs2.io.file.Path("assistant.txt"))
    a.use { assistant =>
      // assistant
      //   .create("gpt-4o-mini")
      //   .flatMap(IO.println) >>
      assistant.assistants().flatMap(IO.println) >>
        assistant.getAssisstant("asst_7g7FJuGyXC8mXGXUg0fTMuOa").map(println)
    }

  end run

end Assistant

Assistant.main(Array.empty)

// val api = AssistantApi.defaultAuthLogToFileAddHeader(fs2.io.file.Path("assistant.txt")).allocated.map(_._1).unsafeRunSync()

// //val create = api.createAssistant("gpt-4o").unsafeRunSync()

// val assistants = api.assistants().unsafeRunSync()

// val assistant = api.getAssisstant(assistants.data.head.id).unsafeRunSync()



```