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

import weather.WeatherService
import weather.WeatherOut
import scala.annotation.experimental

// @experimental
// object ReplClient:

//   import cats.effect.unsafe.implicits.global

//   lazy val openAiUrl = Uri.unsafeFromString("https://api.openai.com/v1")

//   lazy val defaultLogger = Some((cIn: Client[IO]) =>
//     Logger(
//       logBody = true,
//       logHeaders = true,
//       (name => name.toString.toLowerCase.contains("token")),
//       Some((x: String) => IO.println(x))
//     )(cIn)
//   )

//   def apply(logger: Option[Client[cats.effect.IO] => Client[IO]] = defaultLogger): openai.App.OpenAIService[cats.Id] =
//     val keyR = env("OPEN_API_TOKEN").as[String].load[IO].toResource
//     val clientR: Resource[cats.effect.IO, Client[cats.effect.IO]] =
//       EmberClientBuilder.default[IO].build

//     val resourced: Resource[IO, openai.App.OpenAIService[IO]] =
//       logger match
//         case None =>
//           for
//             key <- keyR
//             client <- clientR
//             openAICS <- SimpleRestJsonBuilder
//               .apply(OpenAIService)
//               .client(authMiddleware(key)(client))
//               .uri(openAiUrl)
//               .resource
//           yield openAICS
//         case Some(logger0) =>
//           for
//             key <- keyR
//             client <- clientR
//             openAICS <- SimpleRestJsonBuilder
//               .apply(OpenAIService)
//               .client(authMiddleware(key)(logger0(client)))
//               .uri(openAiUrl)
//               .resource
//           yield openAICS

//     resourced.allocated.map(_._1).unsafeRunSync().transform(sync)

//   end apply

// end ReplClient
@experimental
object SmithyModelled extends IOApp.Simple:

  import openai.App.FunctionCall
  import openai.App.AiMessage

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

  // val keyR = env("OPEN_API_TOKEN").as[String].load[IO].toResource

  // def functionMessage(functionResult: String, functionName: String): ChatCompletionRequestMessage =
  //   ChatCompletionRequestMessage(
  //     role = "ChatCompletionRequestMessageRole.function",
  //     content = functionResult,
  //     name = functionName.some
  //   )

  def assistentMessageFctCall(result: String): AiMessage =
    AiMessage(
      role = "assistant",
      content = result
    )

  val promptStr = List[String](
    // "Tell me the the weather for Zurich, Switzerland"
    "Get the weather at latitude 47.3769 and longditude 8.5417"
    // "Get the weather at latitude 47.3769 and longditude 8.5417, use the packed tool"
  )

  val prompts = promptStr.map(userMsg)

  val clientR: Resource[cats.effect.IO, Client[cats.effect.IO]] =
    EmberClientBuilder.default[IO].build // .map(logger(_))
  val gpt3Turbo = "gpt-3.5-turbo-0613"
  def run: IO[Unit] =
    App.aiResource.use: (openAI) =>

      val testJiggy = new JsonProtocolF[IO]
      val functions4Bot = testJiggy.openAiApiFunctions(API[WeatherService].liftService(weather.weatherImpl))
      val smithyDispatcher =
        testJiggy.openAiSmithyFunctionDispatch(API[WeatherService].liftService(weather.weatherImpl))

      val sendMe =
        for (aPrompt <- prompts)
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
                functions = Some(functions4Bot)
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
                    val fctCall = botChoices.message.function_call.get
                    val fctResult = smithyDispatcher.apply(fctCall)
                    val fctName = fctCall.name
                    fctResult.map(s =>
                      Some(
                        List(
                          assistentMessageFctCall(smithy4s.json.Json.writeDocumentAsPrettyString(s))
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
    // end resourced

  end run

end SmithyModelled
