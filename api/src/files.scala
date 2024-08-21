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
import org.http4s.ember.client.EmberClientBuilder
import ciris.*
import org.http4s.Uri
import org.http4s.syntax.literals.uri
import org.http4s.Request
import org.http4s.Method
import org.http4s.multipart.Part
import fs2.io.file.Path
import fs2.io.file.Files
import org.http4s.EntityDecoder
import org.http4s.multipart.Multipart
import org.http4s.Headers
import cats.parse.strings.Json

// This is not simple rest json. Hence added as an extension method, can't take adavance of smithy wizardry.
extension (fApi: FilesApi)
  def upload[F[_]: Files](
      baseUrl: String = "https://api.openai.com",
      provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build,
      file: Path,
      purpose: String = "purpose"
  ) =
    val filePart = Part.fileData[IO](
      "file",
      file
    )
    val purposePart = Part.formData[IO](name = purpose, value = "assistants")
    val multipart = Multipart[IO](Vector(filePart, purposePart))

    val req = Request[IO](
      Method.POST,
      Uri.unsafeFromString(baseUrl + "/v1/files"),
      headers = multipart.headers
    ).withEntity(multipart)
    provided
      .use(_.expect[String](req))
      .map(resp =>
        smithy4s.json.Json
          .read[File](Blob(resp))
          .fold(
            ex => throw (ex),
            identity
          )
      )
  end upload
end extension

/** https://platform.openai.com/docs/api-reference/files
  */
@simpleRestJson
trait FilesApi derives API:

  // This needs custom headers - i.e. isn't simple rest json
  def upload(
      purpose: String,
      file: Blob
  ): IO[File] = throw new Exception(
    "This operation is not a json endpoint. Look for the upload method on the companion object of the FilesAPI"
  )

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

  def content(
      @hints(HttpLabel())
      id: String
  ): IO[Blob] = throw new Exception(
    "This operation is not a json endpoint. Look for the method on the companion object of the FilesAPI"
  )

end FilesApi

object FilesApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, FilesApi] =
    SimpleRestJsonBuilder(API.service[FilesApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)

  def defaultAuthLogToFile(
      logPath: fs2.io.file.Path,
      provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  ): Resource[IO, FilesApi] =
    val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
    val logger = fileLogger(logPath)
    for
      _ <- makeLogFile(logPath).toResource
      client <- provided
      authdClient = authMiddleware(apikey)(assistWare(logger(client)))
      chatGpt <- FilesApi.apply((authdClient), uri"https://api.openai.com/")
    yield chatGpt
    end for
  end defaultAuthLogToFile

  // def upload[F[_]: Files](
  //   baseUrl: String = "https://api.openai.com",
  //   provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build,
  //   file: Path,
  //   purpose: String = "purpose"
  // ) =
  //   val filePart = Part.fileData[IO](
  //     "file",
  //     file
  //   )
  //   val purposePart = Part.formData[IO](name = purpose, value = "assistants")
  //   val multipart = Multipart[IO](Vector(filePart, purposePart))

  //   val req = Request[IO](
  //     Method.POST,
  //     Uri.unsafeFromString(baseUrl+"/v1/files"),
  //     headers = multipart.headers
  //   ).withEntity(multipart)
  //   provided.use{ _.expect[String](req)}.map(resp => smithy4s.json.Json.read[File](Blob(resp)).fold(
  //     ex => throw(ex),
  //     identity
  //   )
  //   )
  // end upload

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
