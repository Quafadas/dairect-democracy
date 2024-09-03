package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import ciris.*
import io.github.quafadas.dairect.ChatGpt.AiMessage
import io.github.quafadas.dairect.ChatGpt.AiTokenUsage
import io.github.quafadas.dairect.ChatGpt.ChatResponse
import org.http4s.Entity
import org.http4s.EntityEncoder
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.literals.uri
import smithy.api.*
import smithy4s.*
import smithy4s.Document.*
import smithy4s.deriving.aliases.simpleRestJson
import smithy4s.deriving.aliases.untagged
import smithy4s.deriving.internals.Meta
import smithy4s.deriving.{*, given}
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.json.Json
import smithy4s.schema.*

import scala.annotation.experimental
import scala.annotation.nowarn

case class Compounds(
    strings: List[String],
    mapy: Map[String, String]
) derives Schema

extension (c: ChatGpt)
  def streamRaw(
      messages: List[AiMessage],
      model: String = "gpt-4o-mini",
      temperature: Option[Double] = None,
      tools: Option[Document] = None,
      response_format: Option[AiResponseFormat] = None,
      authdClient: Client[IO],
      baseUrl: String = "https://api.openai.com"
  ): fs2.Stream[IO, List[ChatChunk]] =
    case class StreamChatRequest(
        messages: List[AiMessage],
        model: String = "gpt-4o-mini",
        temperature: Option[Double] = None,
        tools: Option[Document] = None,
        response_format: Option[AiResponseFormat] = None,
        stream: Boolean = true
    ) derives Schema

    val enc: EntityEncoder[IO, StreamChatRequest] =
      EntityEncoder.encodeBy[IO, StreamChatRequest](("Content-Type" -> "application/json"))(scr =>
        Entity(fs2.Stream.emits[IO, Byte](smithy4s.json.Json.writeBlob(scr).toArray))
      )

    val req = Request[IO](
      Method.POST,
      Uri.unsafeFromString(baseUrl + "/v1/chat/completions")
    ).withEntity(
      StreamChatRequest(
        messages,
        model,
        temperature,
        tools,
        response_format
      )
    )(using enc)
    val io =
      IO(
        authdClient
          .stream(
            req
          )
          .flatMap { str =>
            str.bodyText
              .evalMap[IO, List[ChatChunk]] { str =>

                /*
                  This is kind of nasty, open AIs streaming format seems to
                  contain multiple chat chunks in each chunk sent over the wire.
                  I suspect the implementation is unsatisfactory in respect of error handling.
                 */

                // val argy = str.split("\n")
                // println("--> test newline Theory")
                // println(argy.mkString(","))
                // println("--> exit newline ")

                /** chunks arrive as
                  *
                  * """data :{"CHAT_CHUNK_HERE"}
                  *
                  * data :{"ANOTHER_CHAT_CHUNK_HERE"}
                  *
                  * data : [DONE] """
                  *
                  * So \1. String split on empty lines. Nice. 2. Check that we don't have the DONE marker (it's not
                  * valid, JSON so we can't easily parse it) 3. drop empty lines 4. Parse whatever is left 5. Pray for a
                  * miracle. Don't forget the miracle.
                  */
                val parsed = str.split("\n").map(_.drop(6)).filterNot(_.isEmpty()).map { maybeParseable =>
                  if maybeParseable == "[DONE]" then
                    // println("ended")
                    None
                  else
                    Json
                      .read[ChatChunk](Blob(maybeParseable))
                      .fold(
                        throw _,
                        Some(_)
                      )
                  end if
                }
                IO(parsed.flatten.toList)
              }
          }
      )

    fs2.Stream.eval(io).flatten

  end streamRaw

  def stream(
      messages: List[AiMessage],
      model: String = "gpt-4o-mini",
      temperature: Option[Double] = None,
      tools: Option[Document] = None,
      response_format: Option[AiResponseFormat] = None,
      authdClient: Client[IO],
      baseUrl: String = "https://api.openai.com"
  ): fs2.Stream[IO, String] = c
    .streamRaw(
      messages,
      model,
      temperature,
      tools,
      response_format,
      authdClient,
      baseUrl
    )
    .map(_.flatMap(_.choices.flatMap(_.delta.content)))
    .flatMap(fs2.Stream.emits)

  // end stream
end extension

@simpleRestJson
trait ChatGpt derives API:
  /** https://platform.openai.com/docs/api-reference/chat
    */
  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/chat/completions"), 200))
  def chat(
      messages: List[AiMessage],
      model: String = "gpt-4o-mini",
      temperature: Option[Double] = None,
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

object AiResponseFormat:
  def json = AiResponseFormat(AiResponseFormatString.json_object)
  def text = AiResponseFormat(AiResponseFormatString.text)
end AiResponseFormat

case class AiResponseFormat(`type`: AiResponseFormatString, json_schema: Option[Document] = None) derives Schema

@untagged
enum ResponseFormat derives Schema:
  @wrapper case Auto(t: String = "auto")
  case AiResponseFormat(
      `type`: AiResponseFormatString,
      json_schema: Option[StructuredOutput] = None
  )

end ResponseFormat

// https://platform.openai.com/docs/guides/structured-outputs
case class StructuredOutput(
    name: String,
    description: Option[String] = None,
    schema: Option[Document],
    strict: Option[Boolean] = Some(true)
) derives Schema

@nowarn
enum AiResponseFormatString derives Schema:
  case json_schema
  case json_object
  case text
end AiResponseFormatString

case class FunctionCall(name: String, description: Option[String], arguments: Option[String]) derives Schema

case class ToolCall(id: String, `type`: String = "function", function: FunctionCall) derives Schema

case class ChunkDelta(
    content: Option[String],
    tool_calls: Option[List[ToolCall]],
    role: Option[String],
    refusal: Option[String]
) derives Schema

case class ChunkChoice(
    delta: ChunkDelta,
    finish_reason: Option[String],
    index: Int
) derives Schema

case class ChatChunk(
    id: String,
    choices: List[ChunkChoice],
    created: Long,
    model: String,
    service_tier: Option[String],
    system_fingerprint: String,
    `object`: String,
    usage: Option[AiTokenUsage]
) derives Schema
