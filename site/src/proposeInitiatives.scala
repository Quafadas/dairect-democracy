package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Resource
import fs2.io.file.Path
import smithy4s.deriving.API
import smithy4s.deriving.EffectMirror

import scala.annotation.experimental

@experimental
object TryInitiatives extends IOApp.Simple:

  import ChatGpt.AiMessage
  import ChatGpt.ChatGptConfig

  def run: IO[Unit] =
    val logFile: Path = fs2.io.file.Path("log.txt")
    val agent: Resource[IO, ChatGpt] = ChatGpt.defaultAuthLogToFile(logFile)    
    val startMessages: List[AiMessage] = List(
      AiMessage.system(
        """You are an agent of a team that works together to solve user defined problems. Your speciality is the tools to use local operating system functions. These may be helpful to other members of the team. """
      ),
      AiMessage.user(
        "create a temporary directory, once that's done create file in it, with the the text `hello world` in it. Don't ask for help. Once you've finished please create a summary of the work you've done.",
        Some("OS_Guy")
      )
    )

    val params: ChatGptConfig = ChatGptConfig(
      model = "gpt-4o-mini",
      temperature = Some(0.0)
    )

    agent
      .use { agent =>
        val loneWolf = Agent(
          agent,
          startMessages,
          params,
          API[OsTool].liftService(osImpl),
          API.service[OsTool],
          "OS_Guy"
        )

        for
          inits <- Democracy.proposeInitiatives(List(loneWolf))
          _ <- IO.println(inits.mkString("\n"))
          votes <- Democracy.vote(List(loneWolf), inits)
          _ <- IO.println(votes.mkString("\n"))
          outcome <- Democracy.delegate(inits, votes, List(loneWolf))
        yield outcome
        end for

      }
      .flatMap(l => IO.println(l.mkString("\n")))

  end run

end TryInitiatives
