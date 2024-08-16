package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import ciris.*
import io.github.quafadas.dairect.AssistantApi.AnAssistant
import io.github.quafadas.dairect.AssistantApi.CreateAssiantResponse
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.all.uri
import smithy.api.Http
import smithy.api.HttpLabel
import smithy.api.NonEmptyString
import smithy.api.Readonly
import smithy4s.*
import smithy4s.deriving.aliases.*
import smithy4s.deriving.{*, given}
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.schema.Schema

import scala.annotation.experimental

/** https://platform.openai.com/docs/api-reference/assistants/createAssistant
  */
@experimental
@simpleRestJson
trait AssistantApi derives API:

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
      // tools: List[String] = List(),
      name: Option[String] = None,
      description: Option[String] = None,
      instructions: Option[String] = None,
      // tool_resources: Option[Map[String, Any]] = None,
      // metadata: Option[Map[String, String]] = None,
      temperature: Option[Double] = Some(1.0),
      top_p: Option[Double] = Some(1.0)
      // response_format: Option[Either[String, Map[String, Any]]] = None
  ): IO[CreateAssiantResponse]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/assistants"), 200), Readonly())
  def assistants(): IO[List[AnAssistant]]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/assistants/{id}"), 200), Readonly())
  def getAssisstant(
      @hints(HttpLabel())
      id: String
  ): IO[AnAssistant]

end AssistantApi

object AssistantApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, AssistantApi] =
    SimpleRestJsonBuilder(API.service[AssistantApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)

  def defaultAuthLogToFile(
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
  end defaultAuthLogToFile

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
      // tools: List[String],
      // metadata: Map[String, String],
      top_p: Double,
      temperature: Double,
      response_format: String
  ) derives Schema

end AssistantApi
