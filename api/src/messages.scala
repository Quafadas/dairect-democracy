package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import ciris.*
import io.github.quafadas.dairect.MessagesApi.Message
import io.github.quafadas.dairect.MessagesApi.MessageAttachment
import io.github.quafadas.dairect.MessagesApi.MessageDeleted
import io.github.quafadas.dairect.MessagesApi.MessageList
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.all.uri
import smithy.api.Http
import smithy.api.HttpLabel
import smithy.api.HttpQuery
import smithy.api.NonEmptyString
import smithy.api.Readonly
import smithy4s.*
import smithy4s.deriving.aliases.*
import smithy4s.deriving.{*, given}
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.schema.Schema

/** https://platform.openai.com/docs/api-reference/assistants/createAssistant
  */

extension (s: String) def msg = MessageOnThread.StrCase(s)
end extension

@simpleRestJson
trait MessagesApi derives API:

  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/threads/{thread_id}/messages"), 200))
  def create(
      @hints(HttpLabel())
      thread_id: String,
      content: MessageOnThread,
      attachments: Option[List[MessageAttachment]] = None,
      metadata: Option[MessageMetaData] = None,
      role: String = "user"
  ): IO[Message]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/threads/{thread_id}/messages/{message_id}"), 200), Readonly())
  def get(
      @hints(HttpLabel())
      thread_id: String,
      message_id: String
  ): IO[Message]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/threads/{thread_id}/messages"), 200), Readonly())
  def list(
      @hints(HttpLabel())
      thread_id: String,
      @hints(HttpQuery("limit"))
      limit: Option[Int] = None,
      @hints(HttpQuery("order"))
      order: Option[String] = None,
      @hints(HttpQuery("after"))
      after: Option[String] = None,
      @hints(HttpQuery("before"))
      before: Option[String] = None,
      @hints(HttpQuery("run_id"))
      run_id: Option[String] = None
  ): IO[MessageList]

  @hints(Http(NonEmptyString("DELETE"), NonEmptyString("/v1/threads/{id}"), 200))
  def deleteThread(
      @hints(HttpLabel())
      id: String
  ): IO[MessageDeleted]

  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/threads/{thread_id}/messages/{message_id}"), 200))
  def modifyMessage(
      @hints(HttpLabel())
      thread_id: String,
      message_id: String
  ): IO[Message]

end MessagesApi

object MessagesApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, MessagesApi] =
    SimpleRestJsonBuilder(API.service[MessagesApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)

  def defaultAuthLogToFile(
      logPath: fs2.io.file.Path,
      provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  ): Resource[IO, MessagesApi] =
    val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
    val logger = fileLogger(logPath)
    for
      _ <- makeLogFile(logPath).toResource
      client <- provided
      authdClient = authMiddleware(apikey)(assistWare(logger(client)))
      chatGpt <- MessagesApi.apply((authdClient), uri"https://api.openai.com/")
    yield chatGpt
    end for
  end defaultAuthLogToFile

  case class Message(
      id: String,
      `object`: String,
      created_at: Long,
      thread_id: String,
      role: String,
      content: MessageContentList,
      assistant_id: Option[String],
      run_id: Option[String],
      attachments: Option[List[MessageAttachment]],
      metadata: MessageMetaData,
      completed_at: Option[Int],
      incomplete_at: Option[Int]
  ) derives Schema

  case class MessageAttachment(
      file_id: Option[String],
      tools: Option[List[MessagesTool]]
  ) derives Schema

  case class MessageDeleted(
      id: String,
      `object`: String,
      deleted: Boolean
  ) derives Schema

  @discriminated("type")
  enum MessagesTool derives Schema:
    case code_interpreter()
    case file_search()
    case function()
  end MessagesTool

  @discriminated("type")
  enum MessagesToolDelta derives Schema:
    case code_interpreter(index: Option[Int], id: String, code_interpreter: CodeInterpreterToolDelta)
    case file_search(index: Option[Int], id: String)
    case function(index: Option[Int], id: Option[String], function: Option[MessageToolFunctionDelta])
  end MessagesToolDelta

  case class MessageToolFunctionDelta(
      arguments: Option[String]
  ) derives Schema

  case class CodeInterpreterToolDelta(
      input: String,
      output: Option[CodeInterpreterToolOutput]
  ) derives Schema

  case class CodeInterpreterToolOutput(
      index: Option[Int],
      `type`: String,
      image: CodeInterpreterToolOutputImage
  ) derives Schema

  case class CodeInterpreterToolOutputImage(file_id: String) derives Schema
  // @untagged
  // enum MsgSend derives Schema:
  //   @wrapper
  //   case MessageContentList(l: List[MessageContent])

  //   @wrapper
  //   case Raw(s : String)
  // end StringOrMessageList

  // type MessageContentList = List[MessageContent]

  @discriminated("type")
  enum MessageContentDelta derives Schema:
    case image(image_file: ImageDetails, index: Option[Int])
    case image_url(image_url: String, index: Option[Int])
    case text(text: TextValueDelta, index: Option[Int])
    case refusal(refusal: String, index: Option[Int])
  end MessageContentDelta

  case class TextValueDelta(
      value: String,
      annotations: Option[List[AnnotationDelta]]
  ) derives Schema

  case class AnnotationDelta(
      index: Option[Int],
      `type`: String,
      text: String,
      start_index: Long,
      end_index: Long,
      file_citation: FileCitation
  ) derives Schema

  case class FileCitation(
      file_id: String,
      quote: String
  ) derives Schema

  // case class ImageFileDelta(file_id: String, detail: String = "low")  derives Schema
  // case class ImageUrl(`type`: String /* png */, image_url: String)  derives Schema
  // case class TextValueDelta(value: String, annotations: List[Annotation] = List())  derives Schema

  // enum Annotation derives Schema:
  //   case Citation(`type`: String = "file_path", text: String, file_citation: FilePathId, start_index: Int, end_index: Int)
  //   case FilePath(`type`: String = "file_path", text: String, file_path: FilePathId, start_index: Int, end_index: Int)
  // end Annotation

  // case class FilePathId(file_id: String) derives Schema

  case class MessageList(
      `object`: String,
      data: List[Message],
      first_id: Option[String],
      last_id: Option[String],
      has_more: Boolean
  ) derives Schema

  case class MessageDelta(
      id: String,
      `object`: String,
      delta: MessageDeltaDetail
  ) derives Schema

  case class MessageDeltaDetail(
      role: Option[String],
      content: List[MessageContentDelta]
  ) derives Schema

end MessagesApi
