package io.github.quafadas.dairect

import smithy4s.*
import smithy4s.deriving.aliases.*
import smithy4s.deriving.{*, given}
import smithy.api.NonEmptyString
import smithy.api.Http
import cats.effect.IO
import org.http4s.client.Client
import org.http4s.Uri
import cats.effect.kernel.Resource
import smithy4s.http4s.SimpleRestJsonBuilder
import ciris.*
import scala.annotation.experimental
import org.http4s.syntax.literals.uri
import org.http4s.Request
import fs2.io.file.Files

trait UrlReader derives API:

  lazy val client: Client[IO] = ???

  def readUrl(url: String): IO[String] =
    val req = Request[IO](org.http4s.Method.GET, Uri.unsafeFromString(url))
    client.run(req).use { response =>
      response.bodyText.compile.string
    }
  end readUrl

  def downloadFile(url: String, completeFilePath: String) =
    val req = Request[IO](org.http4s.Method.GET, Uri.unsafeFromString(url))
    client.run(req).use { response =>
      response.body.through(Files[IO].writeAll(fs2.io.file.Path(completeFilePath))).compile.drain
    }
  end downloadFile

end UrlReader

object UrlReader:
  def apply(provided: Client[IO]): UrlReader = new UrlReader:
    override lazy val client: Client[IO] = provided
end UrlReader

def serpware: Client[IO] => Client[IO] =
  val tok: Resource[IO, String] = env("SERP_API_TOKEN").as[String].load[IO].toResource
  serpWare(tok)
end serpware

@simpleRestJson
trait Serp derives API:

  @readonly
  @hints(Http(NonEmptyString("GET"), NonEmptyString("/search"), 200))
  def search(
      @httpQuery("q")
      q: String,
      @httpQuery("num")
      num: Int = 5
  ): IO[Document]

end Serp

object Serp:
  def apply(client: Client[IO], baseUrl: Uri = uri"https://serpapi.com/"): Resource[IO, Serp] =
    SimpleRestJsonBuilder(API.service[Serp])
      .client[IO](serpware(client))
      .uri(baseUrl)
      .resource
      .map(_.unliftService)

  def make(client: Client[IO], baseUrl: Uri = uri"https://serpapi.com/"): Serp =
    SimpleRestJsonBuilder(API.service[Serp])
      .client[IO](serpware(client))
      .uri(baseUrl)
      .make
      .fold(
        ex => throw ex,
        service => service.unliftService
      )

end Serp

trait InternetTool derives API:
  lazy val reader: UrlReader = ???
  lazy val search: Serp = ???
  def readUrl(url: String): IO[String] =
    IO.println("reading url at " + url) >>
      reader.readUrl(url)

  def downloadFile(url: String, completeFilePath: String): IO[Unit] =
    IO.println("downloading file from " + url + " to " + completeFilePath) >>
      reader.downloadFile(url, completeFilePath)

  def search(
      @httpQuery("q")
      q: String,
      @httpQuery("num")
      num: Int = 5
  ): IO[Document] =
    IO.println(s"Searching for $q") >>
      search.search(q, num)
  def makeTempDir(dirPrefix: String): IO[String] =
    IO.println("Creating a temporary directory") >>
      IO.blocking {
        val outDir = os.temp.dir(deleteOnExit = false, prefix = dirPrefix).toString
        outDir.toString
      }

  def createOrOverwriteFileInDir(dir: String, fileName: String, contents: Option[String]): IO[String] =
    IO.println(s"Creating a file in $dir") >>
      IO.blocking {
        val filePath = os.Path(dir) / fileName
        os.write.over(filePath, contents.getOrElse(""))
        filePath.toString
      }

end InternetTool

object InternetTool:
  def makeFromClient(client: Client[IO]): InternetTool = new InternetTool:
    override lazy val reader: UrlReader = UrlReader(client)
    override lazy val search: Serp = Serp.make(client)
end InternetTool
