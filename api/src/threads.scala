package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import io.github.quafadas.dairect.ChatGpt.AiMessage
import io.github.quafadas.dairect.ThreadApi.Thread
import io.github.quafadas.dairect.ThreadApi.ThreadDeleted
import io.github.quafadas.dairect.ThreadApi.ToolResources
import io.github.quafadas.dairect.VectorStoreFilesApi.ChunkingStrategy
import org.http4s.Uri
import org.http4s.client.Client
import smithy.api.Http
import smithy.api.HttpLabel
import smithy.api.Idempotent
import smithy.api.NonEmptyString
import smithy.api.Readonly
import smithy4s.*
import smithy4s.deriving.aliases.*
import smithy4s.deriving.{*, given}
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.schema.Schema

/** https://platform.openai.com/docs/api-reference/assistants/createAssistant
  */

@simpleRestJson
trait ThreadApi derives API:

  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/threads"), 200))
  def create(
      messages: List[AiMessage],
      tool_resources: ToolResources,
      metadata: Option[ThreadMetaData] = None
  ): IO[Thread]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/threads/{id}"), 200), Readonly())
  def getThread(
      @hints(HttpLabel())
      id: String
  ): IO[Thread]

  @hints(Http(NonEmptyString("DELETE"), NonEmptyString("/v1/threads/{id}"), 200), Idempotent())
  def deleteThread(
      @hints(HttpLabel())
      id: String
  ): IO[ThreadDeleted]

  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/threads/{id}"), 200))
  def modifyThread(
      @hints(HttpLabel())
      id: String,
      messages: List[AiMessage],
      tool_resources: ToolResources
      // metadata: Option[Map[String, String]] = None,
  ): IO[Thread]

end ThreadApi

object ThreadApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, ThreadApi] =
    SimpleRestJsonBuilder(API.service[ThreadApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)

  // def defaultAuthLogToFile(
  //     logPath: fs2.io.file.Path,
  //     provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  // ): Resource[IO, ThreadApi] =
  //   val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
  //   val logger = fileLogger(logPath)
  //   for
  //     _ <- makeLogFile(logPath).toResource
  //     client <- provided
  //     authdClient = authMiddleware(apikey)(assistWare(logger(client)))
  //     chatGpt <- ThreadApi.apply((authdClient), uri"https://api.openai.com/")
  //   yield chatGpt
  //   end for
  // end defaultAuthLogToFile

  case class Thread(
      id: String,
      `object`: String,
      created_at: Long,
      metadata: ThreadMetaData
      //   tool_resources: Map[String, Any]
  ) derives Schema

  case class ToolResources(
      code_interpreter: CodeInterpreter,
      file_search: FileSearch
  ) derives Schema

  case class CodeInterpreter(
      file_ids: CodeInterpreterFileIds
  ) derives Schema

  final case class ThreadDeleted(id: String, `object`: String, deleted: Boolean) derives Schema

  case class FileSearch(
      vector_store_ids: VectorStoreIds,
      vector_stores: VectorStoreIds
  ) derives Schema

  case class VectorStoreFilesToAttach(
      files_ids: FileIds,
      chunking_strategy: ChunkingStrategy,
      metadata: VectorStoreMetaData
  ) derives Schema

end ThreadApi
