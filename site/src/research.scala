package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Resource
import fs2.io.file.Path
import smithy4s.deriving.API
import smithy4s.deriving.EffectMirror

import scala.annotation.experimental

import org.http4s.ember.client.EmberClientBuilder
import cats.syntax.all.*

object Researcher extends IOApp.Simple:

  import ChatGpt.AiMessage
  import ChatGpt.ChatGptConfig

  def run: IO[Unit] =
    val logFile: Path = fs2.io.file.Path("log.txt")
    val clientR = EmberClientBuilder.default[IO].build
    val bot: Resource[IO, ChatGpt] = ChatGpt.defaultAuthLogToFile(logFile, clientR)
    val serp = clientR.flatMap(c => Serp(fileLogger(logFile)(c)))
    val fetcher = clientR.map(UrlReader(_))

    val startMessages: List[AiMessage] = List(
      AiMessage.system(
        """You are an agent of a team that works together to solve user defined problems"""
      ),
      AiMessage.user(
        "Search the internet for things to do with childeren near Valle de Bravo in mexico city. Once you've found some activities write a summary with the links for each idea."
      )
    )

    val params: ChatGptConfig = ChatGptConfig(
      model = "gpt-4-turbo",
      temperature = Some(0.0)
    )

    (bot, serp, fetcher).parTupled
      .use { (bot, serp, fetcher) =>

        // val osGuy = Agent(bot, startMessages, params, API[OsTool].liftService(osImpl), API.service[OsTool], "os_guy")

        val searchTool = Agent(
          bot,
          startMessages,
          params,
          API[Serp].liftService(serp),
          API.service[Serp],
          "Searcher"
        )
        val fetchTool = Agent(
          bot,
          startMessages,
          params,
          API[UrlReader].liftService(fetcher),
          API.service[UrlReader],
          "Fetcher"
        )

        val agents = List(searchTool, fetchTool)

        for
          inits <- Democracy.proposeInitiatives(agents)
          _ <- IO.println(inits.mkString("\n"))
          votes <- Democracy.vote(agents, inits)
          _ <- IO.println(votes.mkString("\n"))
          outcome <- Democracy.delegate(inits, votes, agents)
        yield outcome
        end for

      }
      .map(l => l.mkString("\n"))

  end run

end Researcher
