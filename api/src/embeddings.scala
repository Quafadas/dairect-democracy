package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import io.github.quafadas.dairect.ChatGpt.AiMessage
import io.github.quafadas.dairect.ChatGpt.ChatResponse
import org.http4s.Uri
import org.http4s.client.Client
import smithy.api.*
import smithy4s.*
import smithy4s.Document.*
import smithy4s.deriving.aliases.simpleRestJson
import smithy4s.deriving.aliases.untagged
import smithy4s.deriving.internals.Meta
import smithy4s.deriving.{*, given}
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.schema.*

import scala.annotation.experimental
import scala.annotation.nowarn
import io.github.quafadas.dairect.EmbeddingsApi.EmbeddingList

@simpleRestJson
trait EmbeddingsApi derives API:
  /** https://platform.openai.com/docs/api-reference/chat
    */
  @hints(Http(NonEmptyString("POST"), NonEmptyString("/v1/embeddings"), 200))
  def embeddings(
      model: String,
      messages: List[AiMessage],
      temperature: Option[Double],
      tools: Option[Document] = None,
      response_format: Option[AiResponseFormat] = None
  ): IO[EmbeddingList]
end EmbeddingsApi

object EmbeddingsApi:
  /** @param client
    *   \- An http4s client - apply your middleware to it
    * @param baseUrl
    *   \- The base url of the openAi service see [[EmbeddingsApi]]
    * @return
    */
  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, EmbeddingsApi] =
    SimpleRestJsonBuilder(API.service[EmbeddingsApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)

  case class Embedding(
      `object`: String,
      embedding: List[Double],
      index: Int
  ) derives Schema

  case class Usage(
      prompt_tokens: Int,
      total_tokens: Int
  ) derives Schema

  case class EmbeddingList(
      `object`: String,
      data: List[Embedding],
      model: String,
      usage: Usage
  ) derives Schema

end EmbeddingsApi
