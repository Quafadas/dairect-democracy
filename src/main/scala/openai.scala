package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import ciris.*
import io.github.quafadas.dairect.ChatGpt.AiMessage
import io.github.quafadas.dairect.ChatGpt.ChatResponse

import io.github.quafadas.dairect.Assistant.CreateAssiantResponse
import io.github.quafadas.dairect.Assistant.AnAssistant

import org.http4s.Uri
import org.http4s.client.Client

import org.http4s.ember.client.EmberClientBuilder

import org.http4s.syntax.all.uri
import smithy.api.*

import smithy4s.*
import smithy4s.Document.*
import smithy4s.deriving.internals.Meta
import smithy4s.deriving.{*, given}
import smithy4s.http4s.SimpleRestJsonBuilder

import smithy4s.schema.*
import smithy4s.json.Json

import scala.annotation.experimental
import smithy4s.deriving.aliases.simpleRestJson

@experimental
@simpleRestJson
trait ChatGpt derives API:
  /** https://platform.openai.com/docs/api-reference/chat
    */
  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/chat/completions"), 200))
  def chat(
      model: String,
      messages: List[AiMessage],
      temperature: Option[Double],
      tools: Option[Document] = None,
      response_format: Option[AiResponseFormat] = None
  ): IO[ChatResponse]
end ChatGpt

object ChatGpt:
  /** @param client
    *   \- An http4s client - apply your middleware to it
    * @param baseUrl
    *   \- The base url of the openAi service see [[ChatGpt]]
    * @return
    */
  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, ChatGpt] = SimpleRestJsonBuilder(API.service[ChatGpt])
    .client[IO](client)
    .uri(baseUrl)
    .resource
    .map(_.unliftService)

  /** It is assumed, that an environment variable OPEN_AI_API_TOKEN is set with a valid token, to openai's api. This
    * agent will write all in and outgoing messages to the file specified (no headers).
    *
    * It ought to be rather easy to customize this to your needs by:
    *   - changing the logPath to a different path ( per agent perhaps )
    *   - using a different URL (if your corp wraps the endpoint seperately)
    *   - Adding other http4s client middleware
    *
    * @param logPath
    *   \- The path to the log file
    * @return
    */
  def defaultAuthLogToFile(
      logPath: fs2.io.file.Path,
      provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  ): Resource[IO, ChatGpt] =
    val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
    val logger = fileLogger(logPath)
    for
      _ <- makeLogFile(logPath).toResource
      client <- provided
      authdClient = authMiddleware(apikey)(logger(client))
      chatGpt <- ChatGpt.apply((authdClient), uri"https://api.openai.com/")
    yield chatGpt
    end for
  end defaultAuthLogToFile

  case class ChatGptConfig(
      model: String,
      temperature: Option[Double],
      responseFormat: Option[AiResponseFormat] = None
  ) derives Schema

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
      choices: List[AiChoice],
      usage: AiTokenUsage
  ) derives Schema

  case class AiTokenUsage(
      completion_tokens: Int,
      prompt_tokens: Int,
      total_tokens: Int
  ) derives Schema

  case class AiMessage(
      role: String,
      content: Option[String] = None,
      tool_calls: Option[List[ToolCall]] = None,
      tool_call_id: Option[String] = None,
      name: Option[String] = None
  ) derives Schema

  type SystemMessage = AiMessage

  type AssisstantMessage = AiMessage

  type ToolMessage = AiMessage

  object AiMessage:
    // Sent by us to the AI
    def user(content: String, name: Option[String] = None): AiMessage =
      AiMessage("user", Some(content), name = name)

    // Seeds the conversation with the AI
    def system(content: String, name: Option[String] = None): SystemMessage =
      AiMessage("system", Some(content), name = name)

    // A message from the AI to us
    def assistant(content: Option[String], tool_calls: List[ToolCall], name: Option[String] = None): AiMessage =
      (content, tool_calls) match
        case (None, x :: xs) =>
          AiMessage("assistant", None, Some(tool_calls), name = name)
        case _ =>
          AiMessage("assistant", content, Some(tool_calls), name = name)

    // A message from a tool to the AI
    def tool(content: String, tool_call_id: String): ToolMessage =
      AiMessage("tool", Some(content), None, Some(tool_call_id))

  end AiMessage

  // https: // platform.openai.com/docs/api-reference/chat
  // type BaseAiMessage = SystemMessage | UserMessage | BotMessage | ToolMessage

  // case class SystemMessage(
  //     content: String,
  //     role: String = "system",
  //     name: Option[String] = None
  // ) derives Schema

  // case class UserMessage(
  //     role: String = "user",
  //     content: String,
  //     name: Option[String] = None
  // ) derives Schema

  // case class BotMessage(
  //     role: String = "assistant",
  //     content: Option[String],
  //     tool_calls: String
  // ) derives Schema

  // case class ToolMessage(
  //     role: String = "tool",
  //     content: String,
  //     tool_call_id: String
  // ) derives Schema

  case class AiChoice(message: AiAnswer, finish_reason: Option[String]) derives Schema

  case class AiAnswer(role: String, content: Option[String], tool_calls: Option[List[ToolCall]]) derives Schema

  case class ToolCall(id: String, `type`: String = "function", function: FunctionCall) derives Schema

  case class FunctionCall(name: String, description: Option[String], arguments: Option[String]) derives Schema

  extension (aic: AiChoice)
    def toMessage: List[AiMessage] =
      List(
        AiMessage.assistant(
          content = aic.message.content,
          tool_calls = aic.message.tool_calls.getOrElse(List.empty)
        )
      )

  case class ChatCompletionFunctions(
      name: String,
      parameters: Document,
      description: Option[String] = None
  ) derives Schema

end ChatGpt

/** https://platform.openai.com/docs/api-reference/assistants/createAssistant
  */
@experimental
@simpleRestJson
trait Assistant derives API:

  /** https://platform.openai.com/docs/api-reference/assistants/createAssistant
    *
    * @param model
    * @param name
    * @param description
    * @param instructions
    * @param tools
    * @param tool_resources
    * @param metadata
    * @param temperature
    * @param top_p
    * @param response_format
    */
  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/assistants"), 200))
  def create(
      model: String,
      name: Option[String] = None,
      description: Option[String] = None,
      instructions: Option[String] = None,
      // tools: Seq[String] = Seq.empty,
      // tool_resources: Option[Map[String, Any]] = None,
      // metadata: Option[Map[String, String]] = None,
      temperature: Option[Double] = Some(1.0),
      top_p: Option[Double] = Some(1.0)
      // response_format: Option[Either[String, Map[String, Any]]] = None
  ): IO[CreateAssiantResponse]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/assistants"), 200))
  def assistants(): IO[List[AnAssistant]]

  def getAssisstant(id: String): IO[AnAssistant]

end Assistant

object Assistant:

  case class AssistantTool(
      `type`: String
  ) derives Schema

  case class AnAssistant(
      id: String,
      `object`: String,
      created_at: Long,
      name: Option[String],
      description: Option[String],
      model: String,
      instructions: Option[String],
      tools: List[AssistantTool],
      // tool_resources: List[String],
      // metadata: Map[String, String],
      top_p: Option[Double],
      temperature: Option[Double],
      response_format: AiResponseFormat
  ) derives Schema

  case class AssistantList(
      `object`: String,
      data: List[AnAssistant],
      first_id: String,
      last_id: String,
      has_more: Boolean
  ) derives Schema

  case class CreateAssiantResponse(
      id: String,
      `object`: String,
      created_at: Long,
      name: Option[String],
      description: Option[String],
      model: String,
      instructions: Option[String],
      tools: List[String],
      metadata: Map[String, String],
      top_p: Double,
      temperature: Double,
      response_format: String
  ) derives Schema

end Assistant

@experimental
object AiResponseFormat:
  def json = AiResponseFormat(AiResponseFormatString.json_object)
  def text = AiResponseFormat(AiResponseFormatString.text)
end AiResponseFormat

@experimental
case class AiResponseFormat(`type`: AiResponseFormatString) //, json_schema: Option[smithy4s.json.Json] = None)
    derives Schema

@experimental
enum AiResponseFormatString derives Schema:
  case json_schema
  case json_object
  case text
end AiResponseFormatString
