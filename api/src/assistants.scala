package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import io.github.quafadas.dairect.AssistantApi.AssistantDeleted
import io.github.quafadas.dairect.AssistantApi.AssistantList
import io.github.quafadas.dairect.AssistantApi.Assistant
import org.http4s.syntax.literals.uri
import org.http4s.client.Client
import smithy.api.Http
import smithy.api.HttpLabel
import smithy.api.NonEmptyString
import smithy.api.Readonly
import smithy4s.*
import smithy4s.deriving.aliases.*
import smithy4s.deriving.{*, given}
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.schema.Schema
import ciris.*

import scala.annotation.experimental
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.Uri
import io.github.quafadas.dairect.AssistantApi.AssistantTool

/** https://platform.openai.com/docs/api-reference/assistants/createAssistant
  */
@simpleRestJson
trait AssistantApi derives API:

  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/assistants"), 200))
  def create(
      model: String,
      tools: List[AssistantTool] = List(),
      name: Option[String] = None,
      description: Option[String] = None,
      instructions: Option[String] = None,
      // tool_resources: Option[Map[String, Any]] = None,
      metadata: Option[AssistantMetaData] = None,
      temperature: Option[Double] = Some(1.0),
      top_p: Option[Double] = Some(1.0),
      @wrapper response_format: Option[ResponseFormat] = None
  ): IO[Assistant]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/assistants"), 200), Readonly())
  def list(): IO[AssistantList]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/assistants/{id}"), 200), Readonly())
  def getAssisstant(
      @hints(HttpLabel())
      id: String
  ): IO[Assistant]

  @hints(Http(NonEmptyString("DELETE"), NonEmptyString("/v1/assistants/{id}"), 200))
  def deleteAssisstant(
      @hints(HttpLabel())
      id: String
  ): IO[AssistantDeleted]

  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/assistants/{id}"), 200))
  def modifyAssisstant(
      @hints(HttpLabel())
      id: String,
      model: String,
      name: Option[String] = None,
      description: Option[String] = None,
      instructions: Option[String] = None,
      temperature: Option[Double] = Some(1.0),
      top_p: Option[Double] = Some(1.0)
  ): IO[Assistant]

end AssistantApi

object AssistantApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, AssistantApi] =
    SimpleRestJsonBuilder(API.service[AssistantApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)

  def defaultAuthLogToFileAddHeader(
      logPath: fs2.io.file.Path,
      provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  ): Resource[IO, AssistantApi] =
    val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
    val logger = fileLogger(logPath)
    for
      _ <- makeLogFile(logPath).toResource
      client <- provided
      authdClient = authMiddleware(apikey)(assistWare(logger(client)))
      chatGpt <- AssistantApi.apply((authdClient), uri"https://api.openai.com/")
    yield chatGpt
    end for
  end defaultAuthLogToFileAddHeader

  case class AssistantList(
      `object`: String,
      data: List[Assistant],
      first_id: String,
      last_id: String,
      has_more: Boolean
  ) derives Schema

  case class Assistant(
      id: String,
      `object`: String,
      created_at: Long,
      name: Option[String],
      description: Option[String],
      model: String,
      instructions: Option[String],
      tools: List[AssistantTool],
      tool_resources: Option[ToolResources],
      metadata: AssistantMetaData,
      top_p: Double,
      temperature: Double,
      response_format: ResponseFormat
  ) derives Schema

  case class AssistantDeleted(
      id: String,
      `object`: String,
      deleted: Boolean
  ) derives Schema

  case class AssistantFileSearch(max_num_results: Option[Int] = None) derives Schema

  case class AssistantToolFunction(
    name: String, 
    parameters:Option[Document],
    description: Option[String] = None,         
    strict: Option[Boolean] = None
  ) derives Schema

  @discriminated("type")
  enum AssistantTool derives Schema {
    case code_interpreter()
    case file_search(file_search : AssistantFileSearch = AssistantFileSearch())
    case function(function : AssistantToolFunction)
  }

end AssistantApi
