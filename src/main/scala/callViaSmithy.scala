package smithyOpenAI

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
import openai.*
import smithy4s.*
import smithy4s.deriving.{given, *}
import smithy4s.deriving.aliases.* // for syntactically pleasant annotations

import scala.annotation.experimental
import openai.App.FunctionCall
import openai.App.AiMessage

@experimental
object Showcase extends IOApp.Simple:

  val prompts = List[String](
    "Get the weather at latitude 47.3769 and longditude 8.5417" // use with the weather service
  )

  val osPrompt = List[String](
    "create a temporary directory, prefixed with `bob`. Once tell me it's path." // use with the os service
  )

  val clientR: Resource[cats.effect.IO, Client[cats.effect.IO]] =
    EmberClientBuilder.default[IO].build
  val gpt3Turbo = "gpt-3.5-turbo-0613"
  def run: IO[Unit] =
    App.aiResource.use: (openAI) =>

      val smithyKitForAI = new SmithyOpenAIUtil[IO]
      val functions4Bot = smithyKitForAI.toJsonSchema(API[OsService].liftService(osImpl))
      val smithyDispatcher =
        smithyKitForAI.openAiSmithyFunctionDispatch(API[OsService].liftService(osImpl))

      val sendMe =
        for (aPrompt <- osPrompt.map(userMsg))
          yield
            val startMessages: List[AiMessage] = List(
              AiMessage(
                role = "system",
                content = "You are a helpful assistent."
              )
            ) :+ aPrompt

            openAI
              .chat(
                model = gpt3Turbo,
                temperature = Some(0.0),
                messages = startMessages,
                tools = Some(functions4Bot)
              )
              .flatMap { response =>
                val botChoices = response.choices.head
                val responseMsg = botChoices.message
                botChoices.finish_reason match
                  case None =>
                    IO.println("-----------------") >>
                      IO.println("Don't want to be here") >>
                      IO.println(response) >>
                      IO.pure(None)
                  case Some(value) =>
                    println(botChoices)
                    val fctCalls = botChoices.message.tool_calls.getOrElse(List.empty)
                    val fctResult = fctCalls.traverse(fct =>
                      smithyDispatcher.apply(fct.function).map { result =>
                        Document.DObject(Map("id" -> Document.fromString(fct.id), "result" -> result))
                      }
                    )
                    // val fctName = fctCall.name
                    fctResult.map(s =>
                      Some(
                        List(
                          assistentMessageFctCall(smithy4s.json.Json.writeDocumentAsPrettyString(Document.array(s)))
                        )
                      )
                    )
                end match
              }
              .flatMap((in: Option[List[AiMessage]]) =>
                in match
                  case None => IO.println("Done")
                  case Some(ccm) =>
                    val newMessages = startMessages ++ ccm
                    openAI
                      .chat(
                        model = gpt3Turbo,
                        temperature = 0.0.some,
                        messages = newMessages
                      )
                      .flatMap(IO.println)
              )

      sendMe.sequence.void
  end run

  extension (s: String)
    def userMsg = AiMessage(
      role = "user",
      content = s
    )
    def systemMsg = AiMessage(
      role = "system",
      content = s
    )
    // def functionMsg(functionResult: String, functionName: String, args: Option[String] = None) = AiMessage(
    //   role = "function",
    //   content = functionResult,
    //   function_call = Some(FunctionCall(name = functionName, arguments = args))
    // )
  end extension

  def assistentMessageFctCall(result: String): AiMessage =
    AiMessage(
      role = "assistant",
      content = result
    )

end Showcase
