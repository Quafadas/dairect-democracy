package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import io.github.quafadas.dairect.RunApi.CreateThread
import io.github.quafadas.dairect.RunApi.Run
import io.github.quafadas.dairect.RunApi.RunList
import io.github.quafadas.dairect.RunApi.ToolChoiceInRun
import io.github.quafadas.dairect.RunApi.ToolOutput
import io.github.quafadas.dairect.RunApi.TruncationStrategy
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

/** https://platform.openai.com/docs/api-reference/run-steps/getRunStep
  */

@simpleRestJson
trait RunApi derives API:

  @hints(
    Http(NonEmptyString("POST"), NonEmptyString("v1/threads/runs"), 200)
  )
  def createThreadAndRun(
      assistant_id: String,
      thread: CreateThread,
      model: Option[String] = None,
      instructions: Option[String] = None,
      tools: Option[List[AssistantTool]] = None,
      tool_resources: Option[ToolResources] = None,
      metadata: Option[RunMetaData] = None,
      temperature: Option[Double] = None,
      top_p: Option[Double] = None,
      max_prompt_tokens: Option[Long] = None,
      max_completion_tokens: Option[Long] = None,
      truncation_strategy: Option[TruncationStrategy] = None,
      tool_choice: Option[ToolChoiceInRun] = None,
      parallel_tool_calls: Option[Boolean] = None,
      response_format: Option[ResponseFormat] = None
  ): IO[Run]

  @hints(
    Http(NonEmptyString("POST"), NonEmptyString("v1/threads/{thread_id}/runs"), 200)
  )
  def create(
      @hints(HttpLabel())
      thread_id: String,
      assistant_id: String,
      model: Option[String],
      instructions: Option[String],
      additional_instructions: Option[String],
      additional_messages: Option[List[ThreadMessage]],
      tools: Option[List[AssistantTool]],
      metadata: RunMetaData,
      temperature: Option[Double],
      top_p: Option[Double],
      max_prompt_tokens: Option[Long],
      max_completion_tokens: Option[Long],
      truncation_strategy: Option[TruncationStrategy],
      tool_choice: Option[ToolChoiceInRun],
      parallel_tool_calls: Option[Boolean],
      response_format: ResponseFormat
  ): IO[Run]

  @hints(
    Http(NonEmptyString("GET"), NonEmptyString("v1/threads/{thread_id}/runs"), 200),
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
  ): IO[RunList]

  @hints(
    Http(NonEmptyString("GET"), NonEmptyString("v1/threads/{thread_id}/runs/{run_id}/cancel"), 200),
    Readonly()
  )
  def cancel(
      @hints(HttpLabel())
      thread_id: String,
      @hints(HttpLabel())
      run_id: String
  ): IO[Run]

  @hints(
    Http(NonEmptyString("GET"), NonEmptyString("v1/threads/{thread_id}/runs/{run_id}"), 200),
    Readonly()
  )
  def get(
      @hints(HttpLabel())
      thread_id: String,
      @hints(HttpLabel())
      run_id: String
  ): IO[Run]

  @hints(
    Http(NonEmptyString("POST"), NonEmptyString("v1/threads/{thread_id}/runs/{run_id}"), 200)
  )
  def modify(
      @hints(HttpLabel())
      thread_id: String,
      @hints(HttpLabel())
      run_id: String,
      metadata: RunMetaData
  ): IO[Run]

  @hints(
    Http(NonEmptyString("POST"), NonEmptyString("v1/threads/{thread_id}/runs/{run_id}/submit_tool_outputs"), 200)
  )
  def submitToolOutput(
      @hints(HttpLabel())
      thread_id: String,
      @hints(HttpLabel())
      run_id: String,
      tool_outputs: List[ToolOutput]
  ): IO[Run]

end RunApi

object RunApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, RunApi] =
    SimpleRestJsonBuilder(API.service[RunApi])
      .client[IO](client)
      .uri(baseUrl)
      .resource
      .map(_.unliftService)
  end apply

  case class Usage(
      prompt_tokens: Int,
      completion_tokens: Int,
      total_tokens: Int
  ) derives Schema

  case class TruncationStrategy(
      `type`: String,
      last_messages: Option[String]
  ) derives Schema

  case class RequiredAction(
      `type`: String = "submit_tool_outputs",
      submit_tool_outputs: List[ToolCall]
  ) derives Schema

  case class RunError(
      code: RunErrorType,
      message: String
  ) derives Schema

  enum RunErrorType derives Schema:
    case ServerError, RateLimitExceeded, InvalidPrompt
  end RunErrorType

  case class RunList(
      `object`: String,
      data: List[Run],
      first_id: String,
      last_id: String,
      has_more: Boolean
  ) derives Schema

  case class Run(
      id: String,
      `object`: String,
      created_at: Long,
      assistant_id: String,
      thread_id: String,
      status: RunStatus,
      required_action: Option[RequiredAction],
      last_error: Option[RunError],
      started_at: Option[Long],
      expires_at: Option[Long],
      cancelled_at: Option[Long],
      failed_at: Option[Long],
      completed_at: Option[Long],
      incomplete_details: Option[RunIncomplete],
      model: String,
      instructions: Option[String],
      tools: List[AssistantTool],
      metadata: RunMetaData,
      usage: Option[Usage],
      temperature: Double,
      top_p: Double,
      max_prompt_tokens: Option[Long],
      max_completion_tokens: Option[Long],
      truncation_strategy: TruncationStrategy,
      tool_choice: ToolChoiceInRun,
      parallel_tool_calls: Boolean,
      response_format: ResponseFormat
  ) derives Schema

  enum RunStatus derives Schema:
    case queued, in_progress, requires_action, cancelling, cancelled, failed, completed, incomplete, expired
  end RunStatus

  case class RunIncomplete(reason: String) derives Schema

  @untagged()
  enum ToolChoiceInRun derives Schema:
    @wrapper() case ToolTreatmentW(tt: ToolTreatment)
    case RunToolDetail
  end ToolChoiceInRun

  case class ToolTreatmentW(
      tt: ToolTreatment
  ) derives Schema

  enum ToolTreatment derives Schema:
    case auto, none, required
  end ToolTreatment

  case class RunToolDetail(
      `type`: String,
      function: RunToolName
  ) derives Schema

  case class RunToolName(name: String) derives Schema

  case class ToolOutput(
      tool_call_id: String,
      output: String
  ) derives Schema

  case class CreateThread(
      messages: List[ThreadMessage],
      tool_resources: Option[ToolResources] = None,
      metadata: Option[ThreadMetaData] = None
  ) derives Schema

end RunApi
