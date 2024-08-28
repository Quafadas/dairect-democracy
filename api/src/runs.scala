package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import io.github.quafadas.dairect.RunsApi.CreateThread
import io.github.quafadas.dairect.RunsApi.Run
import io.github.quafadas.dairect.RunsApi.RunList
import io.github.quafadas.dairect.RunsApi.ToolChoiceInRun
import io.github.quafadas.dairect.RunsApi.ToolOutput
import io.github.quafadas.dairect.RunsApi.TruncationStrategy
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
trait RunsApi derives API:

  @hints(
    Http(NonEmptyString("POST"), NonEmptyString("v1/threads/runs"), 200)
  )
  def createThreadAndRun(
      assistant_id: String,
      thread: Option[CreateThread],
      model: Option[String],
      instructions: Option[String],
      tools: Option[List[AssistantTool]],
      tool_resources: Option[ToolResources],
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

end RunsApi

object RunsApi:

  def apply(client: Client[IO], baseUrl: Uri): Resource[IO, RunsApi] =
    SimpleRestJsonBuilder(API.service[RunsApi])
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
      started_at: Long,
      expires_at: Option[Long],
      cancelled_at: Option[Long],
      failed_at: Option[Long],
      completed_at: Option[Long],
      incomplete_details: Option[RunIncomplete],
      model: String,
      instructions: Option[String],
      tools: List[AssistantTool],
      metadata: RunMetaData,
      usage: Usage,
      temperature: Double,
      top_p: Double,
      max_prompt_tokens: Int,
      max_completion_tokens: Int,
      truncation_strategy: TruncationStrategy,
      tool_choice: ToolChoiceInRun,
      parallel_tool_calls: Boolean,
      response_format: ResponseFormat
  ) derives Schema

  enum RunStatus derives Schema:
    case Queued, InProgress, RequiresAction, Cancelling, Cancelled, Failed, Completed, Incomplete, Expired
  end RunStatus

  case class RunIncomplete(reason: String) derives Schema

  enum ToolChoiceInRun derives Schema:
    case ToolTreatment
    case RunToolDetail
  end ToolChoiceInRun

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

end RunsApi
