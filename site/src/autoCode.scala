package io.github.quafadas.dairect

import cats.effect.IOApp
import cats.effect.IO
import smithy4s.deriving.*

import cats.syntax.option.*

import scala.language.experimental
import smithy4s.*
import scala.annotation.experimental

import fs2.io.file.Path
import cats.effect.kernel.Resource

@experimental
object AutoCodeExample extends IOApp.Simple:

  import ChatGpt.AiMessage

  import ChatGpt.ChatGptConfig

  def run: IO[Unit] =
    val logFile: Path = Path("log.txt")
    val agent: Resource[IO, ChatGpt] = ChatGpt.defaultAuthLogToFile(logFile)

    agent.use { agent =>
      val startMessages: List[AiMessage] = List(
        AiMessage.system(
          "You are a helpful assistent. You write code in scala to solve problems. You have access to scala-cli via the compileScalaDir and runScalaDir functions, which will compile and run scala code in a given directory"
        ),
        AiMessage.user(
          "Write a scala script to download data from https://raw.githubusercontent.com/uwdata/draco/master/data/cars.csv to a temporary directory. Tell me the first 5 rows of the data."
        )
      )

      val params = ChatGptConfig(
        model = "gpt-3.5-turbo-0613",
        temperature = 0.0.some
      )
      Agent.startAgent(agent, startMessages, params, API[AutoCode].liftService(autoCodeable)).void
    }

  end run

end AutoCodeExample

@experimental
object StockPrices extends IOApp.Simple:

  import ChatGpt.AiMessage

  import ChatGpt.ChatGptConfig

  def run: IO[Unit] =
    val logFile: Path = Path("log.txt")
    val agent: Resource[IO, ChatGpt] = ChatGpt.defaultAuthLogToFile(logFile)
    val startMessages: List[AiMessage] = List(
      AiMessage.system(
        """
You are a helpful assistent. You write code in scala to solve problems.
You have access to scala-cli via the compileScalaDir and runScalaDir functions, which will compile and run scala code in a given directory.
Remember to include a project.scala file, which sets out the libraries you need to import.
Don't forget to wriote
"""
      ),
      AiMessage.user(
        "Write a scala script to find the year-to-date gain for META and TESLA using the Alpha Vantage API (the environment variable ALPHA_VANTAGE_API_KEY has the API key) then compare the year to date returns"
      )
    )

    val params: ChatGptConfig = ChatGptConfig(
      model = "gpt-4o",
      temperature = Some(0.0)
    )

    agent
      .use { agent =>
        Agent.startAgent(agent, startMessages, params, API[AutoCode].liftService(autoCodeable))
      }
      .flatMap(l => IO.println(l.mkString("\n")))

  end run

end StockPrices
