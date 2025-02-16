package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import io.github.quafadas.dairect.ChatGpt.AiTokenUsage
import io.github.quafadas.dairect.RunStepsApi.RunStep
import io.github.quafadas.dairect.RunStepsApi.RunStepList
import org.http4s.Uri
import org.http4s.client.Client
import smithy.api.Http
import smithy.api.HttpLabel
import smithy.api.HttpQuery
import smithy.api.NonEmptyString
import smithy.api.Readonly
import smithy4s.*
import smithy4s.deriving.aliases.*
import smithy4s.deriving.{*, given}
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.schema.Schema
import io.github.quafadas.dairect.MessagesApi.MessagesToolDelta

/** https://platform.openai.com/docs/api-reference/run-steps/getRunStep
  */

@simpleRestJson
trait RunStepsApi derives API:

  @hints(
    Http(NonEmptyString("GET"), NonEmptyString("v1/threads/{thread_id}/runs/{run_id}/steps"), 200),
    Readonly()
  )
  def list(
      @hints(HttpLabel())
      thread_id: String,
      @hints(HttpLabel())
      run_id: String,
      @hints(HttpQuery("limit"))
      limit: Option[Int],
      @hints(HttpQuery("order"))
      order: Option[String],
      @hints(HttpQuery("after"))
      after: Option[String],
      @hints(HttpQuery("before"))
      before: Option[String]
  ): IO[RunStepList]

  @hints(
    Http(NonEmptyString("GET"), NonEmptyString("v1/threads/{thread_id}/runs/{run_id}/steps/{step_id}"), 200),
    Readonly()
  )
  def get(
      @hints(HttpLabel())
      thread_id: String,
      @hints(HttpLabel())
      run_id: String,
      @hints(HttpLabel())
      step_id: String
  ): IO[RunStep]

end RunStepsApi

object RunStepsApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, RunStepsApi] =
    SimpleRestJsonBuilder(API.service[RunStepsApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)
  end apply

  case class MessageCreationDetails(
      message_id: String
  ) derives Schema

  case class StepDetails(
      `type`: String,
      message_creation: Option[MessageCreationDetails]
  ) derives Schema

  case class RunStep(
      id: String,
      `object`: String,
      created_at: Long,
      run_id: String,
      assistant_id: String,
      thread_id: String,
      `type`: String,
      status: String,
      cancelled_at: Option[Long],
      completed_at: Option[Long],
      expired_at: Option[Long],
      failed_at: Option[Long],
      last_error: Option[LastError],
      step_details: Option[StepDetails],
      usage: Option[AiTokenUsage]
  ) derives Schema

  case class LastError(
      code: String,
      message: String
  ) derives Schema

  case class RunStepList(
      `object`: String,
      data: List[RunStep],
      first_id: String,
      last_id: String,
      has_more: Boolean
  ) derives Schema

  case class RunStepDelta(
      id: String,
      `object`: String,
      delta: RunStepDeltaDetail
  ) derives Schema

  case class RunStepDeltaDetail(
      step_details: RunStepDeltaDetails
  ) derives Schema

  @discriminated("type")
  enum RunStepDeltaDetails derives Schema:
    case tool_calls(tool_calls: List[MessagesToolDelta])
    case message_creation(message_creation: MessageCreation)
  end RunStepDeltaDetails

  case class MessageCreation(
      id: String
  ) derives Schema

end RunStepsApi
