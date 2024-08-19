package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import io.github.quafadas.dairect.ChatGpt.AiMessage
import io.github.quafadas.dairect.ThreadApi.Thread
import io.github.quafadas.dairect.ThreadApi.ThreadDeleted
import io.github.quafadas.dairect.ThreadApi.ToolResources
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
import io.github.quafadas.dairect.VectorStoreApi.VectorStore
import io.github.quafadas.dairect.VectorStoreFilesApi.ChunkingStrategy
import io.github.quafadas.dairect.VectorStoreApi.VectorStoreList
import io.github.quafadas.dairect.VectorStoreApi.DeletedVectorStore

/** https://platform.openai.com/docs/api-reference/vector-stores
  */

@simpleRestJson
trait VectorStoreApi derives API:

  /** https://platform.openai.com/docs/api-reference/vector-stores/create
    */
  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/vector_stores"), 200))
  def create(
      name: Option[String],
      expires_after: Option[String],
      chunkingStrategy: Option[ChunkingStrategy] = None
  ): IO[VectorStore]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/vector_stores"), 200), Readonly())
  def list(): IO[VectorStoreList]

  @hints(
    Http(NonEmptyString("GET"), NonEmptyString("/v1/vector_stores/{vector_store_id}"), 200),
    Readonly()
  )
  def get(
      @hints(HttpLabel())
      vector_store_id: String,
      @hints(HttpLabel())
      file_id: String
  ): IO[VectorStore]

  @hints(
    Http(NonEmptyString("DELETE"), NonEmptyString("/v1/vector_stores/{vector_store_id}"), 200),
    Idempotent()
  )
  def deleteVectorStoreFile(
      @hints(HttpLabel())
      vector_store_id: String,
      @hints(HttpLabel()) file_id: String
  ): IO[DeletedVectorStore]

end VectorStoreApi

object VectorStoreApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, VectorStoreApi] =
    SimpleRestJsonBuilder(API.service[VectorStoreApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)

  // def defaultAuthLogToFile(
  //     logPath: fs2.io.file.Path,
  //     provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  // ): Resource[IO, VectorStoreApi] =
  //   val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
  //   val logger = fileLogger(logPath)
  //   for
  //     _ <- makeLogFile(logPath).toResource
  //     client <- provided
  //     authdClient = authMiddleware(apikey)(assistWare(logger(client)))
  //     chatGpt <- VectorStoreApi.apply((authdClient), uri"https://api.openai.com/")
  //   yield chatGpt
  //   end for
  // end defaultAuthLogToFile

  case class FileCounts(
      in_progress: Int,
      completed: Int,
      failed: Int,
      cancelled: Int,
      total: Int
  ) derives Schema

  case class VectorStore(
      id: String,
      `object`: String,
      created_at: Long,
      name: String,
      bytes: Long,
      file_counts: FileCounts
  ) derives Schema

  case class VectorStoreList(
      `object`: String,
      data: List[VectorStore],
      first_id: String,
      last_id: String,
      has_more: Boolean
  ) derives Schema

  case class DeletedVectorStore(
      id: String,
      `object`: String,
      deleted: Boolean
  ) derives Schema

end VectorStoreApi
