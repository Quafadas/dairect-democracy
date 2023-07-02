package smithyOpenAI

import cats.effect.kernel.Resource
import org.http4s.client.Client
import smithy4s.http.json.JCodec
import cats.effect.IOApp
import cats.effect.IO
import smithy4s.schema.Schema
import smithy4s.Document
import org.http4s.client.middleware.Logger
import org.http4s.ember.client.EmberClientBuilder
import ciris.*
import smithy4s.http4s.SimpleRestJsonBuilder
import openAI.OpenAIService
import org.http4s.Uri
import openAI.OpenAIServiceGen
import org.http4s.headers.Authorization
import org.http4s.Credentials
import org.http4s.AuthScheme

import openAI.CreateChatCompletionRequest
import cats.syntax.option.*

import openAI.ChatCompletionRequestMessage
import openAI.ChatCompletionRequestMessageRole
import org.http4s.Headers
import org.http4s.Header
import org.typelevel.ci.CIStringSyntax
import org.http4s.headers.Accept
import org.http4s.headers.Authorization
import org.http4s.MediaRange

import weather.JsonProtocolF
import weather.weatherServiceImpl

object SmithyModelled extends IOApp.Simple:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  val keyR = env("OPEN_API_TOKEN").as[String].load[IO].toResource

  val logger = (cIn: Client[IO]) =>
    Logger(logBody = true, logHeaders = true, _ => false, Some((x: String) => IO.println(x)))(cIn)

  def authMiddleware(inTok: String): org.http4s.client.Middleware[IO] = (client: Client[IO]) =>
    Client { req =>
      client.run(
        req.putHeaders(
          Authorization(Credentials.Token(AuthScheme.Bearer, inTok))
        )
      )
    }

  val clientR: Resource[cats.effect.IO, Client[cats.effect.IO]] =
    EmberClientBuilder.default[IO].build // .map(logger(_))

  def run =
    val resourced: Resource[IO, OpenAIService[IO]] = for
      key <- keyR
      client <- clientR
      openAICS <- SimpleRestJsonBuilder
        .apply(OpenAIService)
        .client(authMiddleware(key)(logger(client)))
        .uri(Uri.unsafeFromString("https://api.openai.com/v1"))
        .resource
    yield openAICS

    val testJiggy = new JsonProtocolF[IO]
    val smithyDispatcher = testJiggy.openAiFunctionDispatch(weatherServiceImpl)
    val tools = testJiggy.toJsonSchema(weatherServiceImpl)

    resourced.use: (openAI) =>
      openAI
        .createChatCompletion(
          CreateChatCompletionRequest(
            model = "gpt-3.5-turbo-0613",
            temperature = 0.0.some,
            messages = List(
              ChatCompletionRequestMessage(
                role = ChatCompletionRequestMessageRole.system,
                content = "You are a helpful assistent."
              ),
              ChatCompletionRequestMessage(
                role = ChatCompletionRequestMessageRole.user,
                content = "Tell me a joke."
              )
            )
            // functions = Stronglyt typed or stringly typed?

          )
        )
        .flatMap { (x) =>
          IO.println(x)
        }

  end run

end SmithyModelled
