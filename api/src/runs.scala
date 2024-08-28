
package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource

import org.http4s.Uri
import org.http4s.client.Client
import smithy.api.Http
import smithy.api.HttpLabel

import smithy.api.NonEmptyString
import smithy.api.Readonly
import smithy4s.*
import smithy4s.deriving.aliases.*
import smithy4s.deriving.{*, given}
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.schema.Schema
import io.github.quafadas.dairect.RunStepsApi.RunStepList
import smithy.api.HttpQuery
import io.github.quafadas.dairect.ChatGpt.AiTokenUsage
import io.github.quafadas.dairect.RunStepsApi.RunStep

/** https://platform.openai.com/docs/api-reference/run-steps/getRunStep
  */

@simpleRestJson
trait RunsApi derives API:

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
      before: Option[String],

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

  enum RunErrorType derives Schema {
    case ServerError, RateLimitExceeded, InvalidPrompt
  }

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
    model: String,
    instructions: Option[String],
    // tools: List[Tool],
    metadata: RunMetaData,
    incomplete_details: Option[String],
    usage: Usage,
    temperature: Double,
    top_p: Double,
    max_prompt_tokens: Int,
    max_completion_tokens: Int,
    truncation_strategy: TruncationStrategy,
    response_format: String,
    tool_choice: String,
    parallel_tool_calls: Boolean
  ) derives Schema

  enum RunStatus derives Schema {
    case Queued, InProgress, RequiresAction, Cancelling, Cancelled, Failed, Completed, Incomplete, Expired
  }

end RunsApi
