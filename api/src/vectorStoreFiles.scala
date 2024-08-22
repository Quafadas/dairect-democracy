package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import io.github.quafadas.dairect.VectorStoreFilesApi.ChunkingStrategy
import io.github.quafadas.dairect.VectorStoreFilesApi.DeletedVectorStoreFile
import io.github.quafadas.dairect.VectorStoreFilesApi.VectorStoreFile
import io.github.quafadas.dairect.VectorStoreFilesApi.VectorStoreFileList
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
import org.http4s.ember.client.EmberClientBuilder
import ciris.*
import org.http4s.syntax.literals.uri

/** https://platform.openai.com/docs/api-reference/vector-stores-files
  */

@simpleRestJson
trait VectorStoreFilesApi derives API:

  /** https://platform.openai.com/docs/api-reference/vector-stores-files/createFile
    */
  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/vector_stores/{vector_store_id}/files"), 200))
  def create(
      @hints(HttpLabel()) vector_store_id: String,
      file_id: String,
      chunkingStrategy: Option[ChunkingStrategy] = None
  ): IO[VectorStoreFile]

  /** https://platform.openai.com/docs/api-reference/vector-stores-files/listFiles
    */
  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/vector_stores/{vector_store_id}/files"), 200), Readonly())
  def list(
      @hints(HttpLabel())
      vector_store_id: String
  ): IO[VectorStoreFileList]

  @hints(
    Http(NonEmptyString("GET"), NonEmptyString("/v1/vector_stores/{vector_store_id}/files/{file_id}"), 200),
    Readonly()
  )
  def get(
      @hints(HttpLabel())
      vector_store_id: String,
      @hints(HttpLabel())
      file_id: String
  ): IO[VectorStoreFile]

  @hints(
    Http(NonEmptyString("DELETE"), NonEmptyString("/v1/vector_stores/{vector_store_id}/files/{file_id}"), 200),
    Idempotent()
  )
  def deleteVectorStoreFile(
      @hints(HttpLabel())
      vector_store_id: String,
      @hints(HttpLabel()) file_id: String
  ): IO[DeletedVectorStoreFile]

end VectorStoreFilesApi

object VectorStoreFilesApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, VectorStoreFilesApi] =
    SimpleRestJsonBuilder(API.service[VectorStoreFilesApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)

  def defaultAuthLogToFile(
      logPath: fs2.io.file.Path,
      provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  ): Resource[IO, VectorStoreFilesApi] =
    val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
    val logger = fileLogger(logPath)
    for
      _ <- makeLogFile(logPath).toResource
      client <- provided
      authdClient = authMiddleware(apikey)(assistWare(logger(client)))
      chatGpt <- VectorStoreFilesApi.apply((authdClient), uri"https://api.openai.com/")
    yield chatGpt
    end for
  end defaultAuthLogToFile

  case class StaticChunkingStrategy(
      max_chunk_size_tokens: Int,
      chunk_overlap_tokens: Int
  ) derives Schema

  case class ChunkingStrategy(
      `type`: String,
      static: Option[StaticChunkingStrategy]
  ) derives Schema

  case class VectorStoreFile(
      id: String,
      `object`: String,
      usage_bytes: Long,
      created_at: Long,
      vector_store_id: String,
      status: String,
      last_error: Option[String],
      chunking_strategy: ChunkingStrategy
  ) derives Schema

  case class VectorStoreFileShort(
      id: String,
      `object`: String,
      created_at: Long,
      vector_store_id: String
  ) derives Schema

  case class VectorStoreFileList(
      `object`: String,
      data: List[VectorStoreFileShort],
      first_id: String,
      last_id: String,
      has_more: Boolean
  ) derives Schema

  case class DeletedVectorStoreFile(
      id: String,
      `object`: String,
      deleted: Boolean
  ) derives Schema

end VectorStoreFilesApi
