package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Resource
import fs2.io.file.Path
import smithy4s.deriving.API

import scala.annotation.experimental



@experimental
object Showcase extends IOApp.Simple:

  import ChatGpt.AiMessage
  import ChatGpt.ChatGptConfig

  def run: IO[Unit] =
    val logFile: Path = fs2.io.file.Path("log.txt")
    val agent: Resource[IO, ChatGpt] = ChatGpt.defaultAuthLogToFile(logFile)
    val startMessages: List[AiMessage] = List(
      AiMessage.system("You are a helpful assistent."),
      AiMessage.user(
        "create a temporary directory, once that's done create file in it, with the the text `hello world` in it. Ask if more help is needed, until you get a negative response. Once you've finished please create a summary of the work you've done."
      )
    )

    val params: ChatGptConfig = ChatGptConfig(
      model = "gpt-3.5-turbo-0613",
      temperature = Some(0.0)
    )

    agent
      .use { agent =>
        Agent.startAgent(agent, startMessages, params, API[OsTool].liftService(osImpl))
      }
      .flatMap(l => IO.println(l.mkString("\n")))

  end run

end Showcase
