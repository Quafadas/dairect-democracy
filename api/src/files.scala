package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import io.github.quafadas.dairect.FilesApi.DeletedFile
import io.github.quafadas.dairect.FilesApi.File
import io.github.quafadas.dairect.FilesApi.FileListy
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

/** https://platform.openai.com/docs/api-reference/files
  */

@simpleRestJson
trait FilesApi derives API:

  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/files"), 200))
  def upload(
      purpose: String,
      file: Blob
  ): IO[File]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/files"), 200), Readonly())
  def files(): IO[FileListy]

  @hints(Http(NonEmptyString("GET"), NonEmptyString("/v1/files/{id}"), 200), Readonly())
  def getFile(
      @hints(HttpLabel())
      id: String
  ): IO[File]

  @hints(Http(NonEmptyString("DELETE"), NonEmptyString("/v1/files/{id}"), 200), Idempotent())
  def deleteFile(
      @hints(HttpLabel())
      id: String
  ): IO[DeletedFile]

  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/files/{id}"), 200))
  def content(
      @hints(HttpLabel())
      id: String
  ): IO[Blob]

end FilesApi

object FilesApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, FilesApi] =
    SimpleRestJsonBuilder(API.service[FilesApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)

  // def defaultAuthLogToFile(
  //     logPath: fs2.io.file.Path,
  //     provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  // ): Resource[IO, FilesApi] =
  //   val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
  //   val logger = fileLogger(logPath)
  //   for
  //     _ <- makeLogFile(logPath).toResource
  //     client <- provided
  //     authdClient = authMiddleware(apikey)(assistWare(logger(client)))
  //     chatGpt <- FilesApi.apply((authdClient), uri"https://api.openai.com/")
  //   yield chatGpt
  //   end for
  // end defaultAuthLogToFile

  case class DeletedFile(
      id: String,
      `object`: String,
      deleted: Boolean
  ) derives Schema

  case class File(
      id: String,
      `object`: String,
      bytes: Long,
      created_at: Long,
      filename: String,
      purpose: String
  ) derives Schema

  case class FileListy(
      data: List[File],
      `object`: String
  ) derives Schema

end FilesApi
