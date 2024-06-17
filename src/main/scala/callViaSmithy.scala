package io.github.quafadas.dairect

import cats.effect.kernel.Resource
import org.http4s.client.Client
import cats.effect.IOApp
import cats.effect.IO
import smithy4s.schema.Schema
import smithy4s.Document
import org.http4s.client.middleware.Logger
import org.http4s.ember.client.EmberClientBuilder
import ciris.*
import smithy4s.http4s.SimpleRestJsonBuilder
import org.http4s.Uri
import org.http4s.headers.Authorization
import org.http4s.Credentials
import org.http4s.AuthScheme
import smithy4s.deriving.*

import cats.syntax.option.*

import org.http4s.Headers
import org.http4s.Header
import org.typelevel.ci.CIStringSyntax
import org.http4s.headers.Accept
import org.http4s.headers.Authorization
import org.http4s.MediaRange
import cats.syntax.all.*
import cats.instances.function
import scala.language.experimental
import smithy4s.*
import smithy4s.deriving.{given, *}
import smithy4s.deriving.aliases.* // for syntactically pleasant annotations

import scala.annotation.experimental
import Agent.FunctionCall

import smithy4s.json.Json
import Agent.AiChoice
import Agent.SystemMessage
import Agent.BaseAiMessage
import Agent.BotMessage
import Agent.ToolMessage
import Agent.UserMessage
import Agent.AiMessage
import cats.effect.std.Random
import Agent.ChatGpt
import org.http4s.syntax.all.uri
import io.github.quafadas.dairect.Agent.ChatGptConfig

@experimental
object Showcase extends IOApp.Simple:
  def run: IO[Unit] =
    val logFile = fs2.io.file.Path("log.txt")    
    val agent = ChatGpt.defaultAuthLogToFile(logFile)
    val startMessages: List[AiMessage] = List(        
      AiMessage.system("You are a helpful assistent."),
      AiMessage.user(
      "create a temporary directory, once that's done create file in it, with the the text `hello world` in it. Ask if more help is needed, until you get a negative response. Once you've finished please create a summary of the work you've done."
      )
    )

    val params = ChatGptConfig(
      model = "gpt-3.5-turbo-0613",
      temperature = 0.0.some
    )

    agent.use { agent =>
      Agent.startAgent(agent, startMessages, params, API[OsTool].liftService(osImpl)).void
    }.flatMap(IO.println)

  end run

end Showcase
