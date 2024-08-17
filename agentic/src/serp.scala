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

@simpleRestJson
@experimental
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

@experimental
trait UrlReader derives API:

  lazy val client: Client[IO] = ???

  def readUrl(url: String): IO[String] =
    val req = Request[IO](org.http4s.Method.GET, Uri.unsafeFromString(url))
    client.run(req).use { response =>
      response.bodyText.compile.string
    }
  end readUrl

end UrlReader

object UrlReader:
  def apply(provided: Client[IO]): UrlReader = new UrlReader:
    override lazy val client: Client[IO] = provided
end UrlReader

def serpware: Client[IO] => Client[IO] = authMiddleware(env("SERP_API_TOKEN").as[String].load[IO].toResource)

//FIXME
object Serp:
  def apply(client: Client[IO], baseUrl: Uri = uri"https://serpapi.com/"): Resource[IO, Serp] =
    SimpleRestJsonBuilder(API.service[Serp])
      .client[IO](serpware(client))
      .uri(baseUrl)
      .resource
      .map(_.unliftService)
end Serp
