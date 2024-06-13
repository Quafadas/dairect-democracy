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

import smithy4s.json.Json
import openai.App.AiChoice
import openai.App.SystemMessage
import openai.App.BaseAiMessage
import openai.App.BotMessage
import openai.App.ToolMessage
import openai.App.UserMessage
import openai.App.AiMessage

@experimental
object Showcase extends IOApp.Simple:

  val prompts = List[String](
    "Get the weather at latitude 47.3769 and longditude 8.5417" // use with the weather service
  )

  val osPrompt =
    AiMessage.user(
      "create a temporary directory, once that's done create file in it, with the the text `hello world`. Then ask for further instructions"
    ) // use with the os service

  val clientR: Resource[cats.effect.IO, Client[cats.effect.IO]] =
    EmberClientBuilder.default[IO].build
  val gpt3Turbo = "gpt-3.5-turbo-0613"
  def run: IO[Unit] =
    App.aiResource.use: (openAI) =>

      val smithyKitForAI = new SmithyOpenAIUtil[IO]
      val functions4Bot = smithyKitForAI.toJsonSchema(API[OsService].liftService(osImpl))
      val smithyDispatcher =
        smithyKitForAI.openAiSmithyFunctionDispatch(API[OsService].liftService(osImpl))

      val startMessages: List[AiMessage] = List(
        AiMessage.system("You are a helpful assistent.")
      ) :+ osPrompt

      val talkToAi = fs2.Stream
        .unfoldEval[IO, List[AiMessage], Unit](startMessages) { allMessages =>
          IO.println(allMessages)
          openAI
            .chat(
              model = gpt3Turbo,
              temperature = Some(0.0),
              messages = allMessages,
              tools = Some(functions4Bot)
            )
            .flatMap { response =>
              println(response)
              val botChoices = response.choices.head
              val responseMsg = botChoices.message
              botChoices.finish_reason match
                case None =>
                  IO.raiseError(
                    new Exception("No finish reason provided. Bot should always provide a finish reason")
                  )
                case Some(value) =>
                  value match
                    case "tool_calls" =>
                      val fctCalls = botChoices.message.tool_calls.getOrElse(List.empty)
                      val newMessages = fctCalls
                        .traverse(fct =>
                          smithyDispatcher.apply(fct.function).map { result =>
                            AiMessage.tool(
                              tool_call_id = fct.id,
                              content = Json.writeDocumentAsPrettyString(result)
                            )
                          }
                        )
                        .map { msgs =>
                          println(allMessages ++ botChoices.toMessage ++ msgs)
                          Some((), allMessages ++ botChoices.toMessage ++ msgs)
                        }
                      newMessages

                    case "stop" =>
                      IO.println("Done") >>
                        IO.pure(None)
                    case _ =>
                      IO.println("Bot finished with unknown finish reason") >>
                        IO.println(response) >>
                        IO.raiseError(new Exception("Bot finished with unknown finish reason"))

              end match
            }

        }

      talkToAi.compile.drain
  end run

  extension (aic: AiChoice)
    def toMessage: List[AiMessage] =
      List(
        AiMessage.assistant(
          content = aic.message.content,
          tool_calls = aic.message.tool_calls.getOrElse(List.empty)
        )
      )

  extension (s: String)
    def userMsg = UserMessage(
      content = s
    )
    def systemMsg = SystemMessage(
      content = s
    )
    // def functionMsg(functionResult: String, functionName: String, args: Option[String] = None) = AiMessage(
    //   role = "function",
    //   content = functionResult,
    //   function_call = Some(FunctionCall(name = functionName, arguments = args))
    // )
  end extension

end Showcase
