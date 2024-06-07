package openai

import smithy4s.*
import smithy4s.deriving.{given, *}
import smithy4s.deriving.aliases.{*, given}
import cats.effect.IO
import scala.annotation.experimental
import org.http4s.ember.client.EmberClientBuilder
import smithy.api.*
import smithy.api.NonEmptyString.asBijection
import smithy4s.http4s.SimpleRestJsonBuilder
import org.http4s.syntax.all.uri
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.AuthScheme
import org.http4s.Headers
import org.http4s.Credentials
import smithy.api.Error.CLIENT
import cats.effect.IOApp
import cats.effect.ExitCode
import ciris.*
import cats.effect.kernel.Resource
import org.http4s.client.middleware.Logger

@experimental
object App extends IOApp:
  import openai.App.AiMessage
  import openai.App.ChatCompletionResponseMessageFunctionCall
  import openai.App.FunctionCall

  val logger = (cIn: Client[IO]) =>
    Logger(
      logBody = true,
      logHeaders = false,
      name => name.toString.toLowerCase.contains("token"),
      Some((x: String) => IO.println(x))
    )(cIn)

  extension (s: String)
    def userMsg = AiMessage(
      role = "user",
      content = s
    )
    def systemMsg = AiMessage(
      role = "system",
      content = s
    )
    def functionMsg(functionResult: String, functionName: String, args: Option[String] = None) = AiMessage(
      role = "function",
      content = functionResult,
      function_call = Some(FunctionCall(name = functionName, arguments = args))
    )
  end extension

  /** This test only that you are hooked into openAI
    *
    * *
    */
  def run(args: List[String]) =
    aiResource
      .use { aiService =>
        aiService
          .chat(
            "gpt-3.5-turbo",
            List(AiMessage("user", "Hello, I am cow. Who are you?")),
            None
          )
          .flatMap(IO.println)
          .handleErrorWith(IO.println)
      }
      .as(ExitCode.Success)

  /** The name and arguments of a function that should be called, as generated by the model.
    * @param name
    *   The name of the function to call.
    * @param arguments
    *   The arguments to call the function with, as generated by the model in JSON format. Note that the model does not
    *   always generate valid JSON, and may hallucinate parameters not defined by your function schema. Validate the
    *   arguments in your code before calling your function.
    */
  case class ChatCompletionResponseMessageFunctionCall(name: Option[String] = None, arguments: Option[String] = None)
      derives Schema

  case class ChatCompletionResponseMessage(
      role: String,
      content: Option[String] = None,
      function_call: Option[ChatCompletionResponseMessageFunctionCall] = None
  ) derives Schema

  case class ChatResponse(
      id: String,
      created: Int,
      model: String,
      choices: List[AiChoice]
  ) derives Schema

  val aiResource = EmberClientBuilder.default[IO].build.flatMap { httpClient =>
    SimpleRestJsonBuilder(API.service[OpenAiService])
      .client[IO](logger(authMiddleware(apikey)(httpClient)))
      .uri(uri"https://api.openai.com/")
      .resource
      .map(_.unliftService)
  }

  case class AiMessage(
      role: String,
      content: String,
      function_call: Option[FunctionCall] = None
  ) derives Schema

  val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource

  case class AiChoice(message: AiAnswer, finish_reason: Option[String]) derives Schema

  case class AiAnswer(role: String, content: Option[String], function_call: Option[FunctionCall]) derives Schema

  case class AiResponseFormat(`type`: String) derives Schema

  case class FunctionCall(name: String, arguments: Option[String]) derives Schema

  def authMiddleware(tokResource: Resource[IO, String]): org.http4s.client.Middleware[IO] = (client: Client[IO]) =>
    Client { req =>
      tokResource.flatMap { tok =>
        client.run(
          req.withHeaders(
            req.headers ++ Headers(Authorization(Credentials.Token(AuthScheme.Bearer, tok)))
          )
        )
      }
    }

  @experimental
  @simpleRestJson
  class OpenAiService() derives API:

    @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/chat/completions"), 200))
    def chat(
        model: String,
        messages: List[AiMessage],
        // responseFormat: AiResponseFormat,
        temperature: Option[Double],
        functions: Option[List[ChatCompletionFunctions]] = None
    ): IO[ChatResponse] = ???
  end OpenAiService

end App

@experimental
case class ChatCompletionFunctions(
    name: String,
    parameters: Document,
    description: Option[String] = None
) derives Schema
