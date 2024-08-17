package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Resource
import cats.syntax.option.*
import smithy4s.deriving.*

import scala.annotation.experimental
import scala.language.experimental


@experimental
object ScalaCliTest extends IOApp.Simple:

  import ChatGpt.AiMessage
  import ChatGpt.ChatGptConfig


  def run: IO[Unit] =
    val logFile = fs2.io.file.Path("log.txt")
    val agent: Resource[IO, ChatGpt] = ChatGpt.defaultAuthLogToFile(logFile)

    agent.use { agent =>
      val startMessages: List[AiMessage] = List(
        AiMessage.system("You are a helpful assistent."),
        AiMessage.user(
          "Compile the code in this directory; /var/folders/b7/r2s8sm653rj8w9krmxd2748w0000gn/T/temp14386784565594745538. Once successfully compiled, run it and provide a summary of your work."
        )
      )

      val params = ChatGptConfig(
        model = "gpt-3.5-turbo-0613",
        temperature = 0.0.some
      )

      Agent.startAgent(agent, startMessages, params, API[ScalaCliTool].liftService(scalaCliImpl)).void
    }
  end run

end ScalaCliTest
