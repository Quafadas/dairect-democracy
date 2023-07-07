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
import cats.syntax.all.*

import weather.JsonProtocolF
import weather.weatherServiceImpl
import openAI.ChatCompletionResponseMessage
import openAI.ChatCompletionRequestMessageFunctionCall
import openAI.CreateChatCompletionResponseChoicesItem

object SmithyModelled extends IOApp.Simple:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  val keyR = env("OPEN_API_TOKEN").as[String].load[IO].toResource

  def userMsg(m: String): ChatCompletionRequestMessage = ChatCompletionRequestMessage(
    role = ChatCompletionRequestMessageRole.user,
    content = m
  )

  def functionMessage(functionResult: String, functionName: String): ChatCompletionRequestMessage =
    ChatCompletionRequestMessage(
      role = ChatCompletionRequestMessageRole.function,
      content = functionResult,
      name = functionName.some
    )

  def assistentMessageFctCall(in: ChatCompletionResponseMessage): ChatCompletionRequestMessage =
    ChatCompletionRequestMessage(
      role = ChatCompletionRequestMessageRole.assistant,
      content = "",
      name = in.function_call.get.name,
      function_call = in.function_call.map(m =>
        ChatCompletionRequestMessageFunctionCall(
          name = m.name.get,
          arguments = m.arguments.get
        )
      )
    )

  val promptStr = List[String](
    "Get the weather for Zurich, Switzerland"
    // "Get the weeather at latitude 47.3769 and longditude 8.5417",
    // "Get the weather at latitude 47.3769 and longditude 8.5417, use the packed tool"
  )

  val prompts = promptStr.map(userMsg)

  val logger = (cIn: Client[IO]) =>
    Logger(logBody = true, logHeaders = false, _ => false, Some((x: String) => IO.println(x)))(cIn)

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

  def run: IO[Unit] =
    val resourced: Resource[IO, OpenAIService[IO]] = for
      key <- keyR
      client <- clientR
      openAICS <- SimpleRestJsonBuilder
        .apply(OpenAIService)
        .client(authMiddleware(key)(logger(client)))
        .uri(Uri.unsafeFromString("https://api.openai.com/v1"))
        .resource
    yield openAICS

    resourced.use: (openAI) =>

      val testJiggy = new JsonProtocolF[IO]
      val functions4Bot = testJiggy.openAiApiFunctions(weatherServiceImpl)
      val smithyDispatcher = testJiggy.openAiSmithyFunctionDispatch(weatherServiceImpl)

      val sendMe =
        for (aPrompt <- prompts)
          yield
            val startMessages: List[ChatCompletionRequestMessage] = List(
              ChatCompletionRequestMessage(
                role = ChatCompletionRequestMessageRole.system,
                content = "You are a helpful assistent."
              )
            ) :+ aPrompt

            openAI
              .createChatCompletion(
                CreateChatCompletionRequest(
                  model = "gpt-3.5-turbo-0613",
                  temperature = 0.0.some,
                  messages = startMessages,
                  functions = functions4Bot.some
                )
              )
              .flatMap { response =>
                val botChoices = response.body.choices.head
                val responseMsg = botChoices.message.get
                botChoices.finish_reason match
                  case None =>
                    IO.println("-----------------") >>
                    IO.println("Don't want to be here") >>
                    IO.println(response) >>
                      IO.pure(None)
                  case Some(value) =>
                    val fctCall = botChoices.message.get.function_call.get
                    val fctResult = smithyDispatcher.apply(fctCall)
                    val fctName = fctCall.name.get
                    fctResult.map(s =>
                      Some(
                        List(
                          assistentMessageFctCall(botChoices.message.get),
                          functionMessage(com.github.plokhotnyuk.jsoniter_scala.core.writeToString(s), fctName)
                        )
                      )
                    )
                end match
              }
              .flatMap((in: Option[List[ChatCompletionRequestMessage]]) =>
                in match
                  case None => IO.println("Done")
                  case Some(ccm) =>
                    val newMessages = startMessages ++ ccm
                    openAI
                      .createChatCompletion(
                        CreateChatCompletionRequest(
                          model = "gpt-3.5-turbo-0613",
                          temperature = 0.0.some,
                          messages = newMessages
                        )
                      )
                      .flatMap(IO.println)
              )

      sendMe.sequence.void
    // end resourced

  end run

end SmithyModelled
