package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import io.github.quafadas.dairect.MessagesApi.Message
import io.github.quafadas.dairect.MessagesApi.MessageDelta
import io.github.quafadas.dairect.RunApi.CreateThread
import io.github.quafadas.dairect.RunApi.Run
import io.github.quafadas.dairect.RunApi.RunList
import io.github.quafadas.dairect.RunApi.StreamRunRequest
import io.github.quafadas.dairect.RunApi.StreamThreadRunRequest
import io.github.quafadas.dairect.RunApi.StreamToolOutput
import io.github.quafadas.dairect.RunApi.ToolChoiceInRun
import io.github.quafadas.dairect.RunApi.ToolOutput
import io.github.quafadas.dairect.RunApi.TruncationStrategy
import io.github.quafadas.dairect.RunStepsApi.RunStep
import io.github.quafadas.dairect.RunStepsApi.RunStepDelta
import io.github.quafadas.dairect.ThreadApi.Thread
import org.http4s.Entity
import org.http4s.EntityEncoder
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.client.Client
import smithy.api.Http
import smithy.api.HttpLabel
import smithy.api.HttpQuery
import smithy.api.NonEmptyString
import smithy.api.Readonly
import smithy4s.*
import smithy4s.codecs.PayloadError
import smithy4s.deriving.aliases.*
import smithy4s.deriving.{*, given}
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.json.Json
import smithy4s.schema.Schema
import org.http4s.ServerSentEvent

// enum AssistantStreamEvent(val id: String) derives Schema:
//   case ThreadCreated(thread: Thread) extends AssistantStreamEvent("thread.created")
//   case ThreadRunCreated(run: Run) extends AssistantStreamEvent("thread.run.created")
//   case ThreadRunQueued(run: Run) extends AssistantStreamEvent("thread.run.queued")
//   case ThreadRunInProgress(run: Run) extends AssistantStreamEvent("thread.run.in_progress")
//   case ThreadRunRequiresAction(run: Run) extends AssistantStreamEvent("thread.run.requires_action")
//   case ThreadRunCompleted(run: Run) extends AssistantStreamEvent("thread.run.completed")
//   case ThreadRunIncomplete(run: Run) extends AssistantStreamEvent("thread.run.incomplete")
//   case ThreadRunFailed(run: Run) extends AssistantStreamEvent("thread.run.failed")
//   case ThreadRunCancelling(run: Run) extends AssistantStreamEvent("thread.run.cancelling")
//   case ThreadRunCancelled(run: Run) extends AssistantStreamEvent("thread.run.cancelled")
//   case ThreadRunExpired(run: Run) extends AssistantStreamEvent("thread.run.expired")
//   case ThreadRunStepCreated(runStep: RunStep) extends AssistantStreamEvent("thread.run.step.created")
//   case ThreadRunStepInProgress(runStep: RunStep) extends AssistantStreamEvent("thread.run.step.in_progress")
//   case ThreadRunStepDelta(runStepDelta: String /*RunStepDelta*/) extends AssistantStreamEvent("thread.run.step.delta")
//   case ThreadRunStepCompleted(runStep: RunStep) extends AssistantStreamEvent("thread.run.step.completed")
//   case ThreadRunStepFailed(runStep: RunStep) extends AssistantStreamEvent("thread.run.step.failed")
//   case ThreadRunStepCancelled(runStep: RunStep) extends AssistantStreamEvent("thread.run.step.cancelled")
//   case ThreadRunStepExpired(runStep: RunStep) extends AssistantStreamEvent("thread.run.step.expired")
//   case ThreadMessageCreated(message: Message) extends AssistantStreamEvent("thread.message.created")
//   case ThreadMessageInProgress(message: Message) extends AssistantStreamEvent("thread.message.in_progress")
//   case ThreadMessageDelta(messageDelta: String /* MessageDelta*/) extends AssistantStreamEvent("thread.message.delta")
//   case ThreadMessageCompleted(message: Message) extends AssistantStreamEvent("thread.message.completed")
//   case ThreadMessageIncomplete(message: Message) extends AssistantStreamEvent("thread.message.incomplete")
//   case Error(error: String) extends AssistantStreamEvent("error")
//   case Done() extends AssistantStreamEvent("done")
//   case Unknown(event: String, data: String) extends AssistantStreamEvent("****")

// @discriminated("object")
// enum AssistantStreamEvent derives Schema:
//   @wrapper case `thread.created`(t: Thread) extends AssistantStreamEvent
//   @wrapper case `thread.run.created`(run: Run) extends AssistantStreamEvent
//   @wrapper case `thread.run.queued`(run: Run) extends AssistantStreamEvent
//   @wrapper case `thread.run.in_progress`(run: Run) extends AssistantStreamEvent
//   @wrapper case `thread.run.requires_action`(run: Run) extends AssistantStreamEvent
//   @wrapper case `thread.run.completed`(run: Run) extends AssistantStreamEvent
//   @wrapper case `thread.run.incomplete`(run: Run) extends AssistantStreamEvent
//   @wrapper case `thread.run.failed`(run: Run) extends AssistantStreamEvent
//   @wrapper case `thread.run.cancelling`(run: Run) extends AssistantStreamEvent
//   @wrapper case `thread.run.cancelled`(run: Run) extends AssistantStreamEvent
//   @wrapper case `thread.run.expired`(run: Run) extends AssistantStreamEvent
//   @wrapper case `thread.run.step.created`(runStep: RunStep) extends AssistantStreamEvent
//   @wrapper case `thread.run.step.in_progress`(runStep: RunStep) extends AssistantStreamEvent
//   @wrapper case `thread.run.step.delta`(runStepDelta: String /*RunStepDelta*/) extends AssistantStreamEvent
//   @wrapper case `thread.run.step.completed`(runStep: RunStep) extends AssistantStreamEvent
//   @wrapper case `thread.run.step.failed`(runStep: RunStep) extends AssistantStreamEvent
//   @wrapper case `thread.run.step.cancelled`(runStep: RunStep) extends AssistantStreamEvent
//   @wrapper case `thread.run.step.expired`(runStep: RunStep) extends AssistantStreamEvent
//   @wrapper case `thread.ressage.created`(message: Message) extends AssistantStreamEvent
//   @wrapper case `thread.ressage.in_progress`(message: Message) extends AssistantStreamEvent
//   @wrapper case `thread.ressage.delta`(messageDelta: String /*MessageDelta*/) extends AssistantStreamEvent
//   @wrapper case `thread.ressage.completed`(message: Message) extends AssistantStreamEvent
//   @wrapper case `thread.ressage.incomplete`(message: Message) extends AssistantStreamEvent
//   @wrapper case error(error: String) extends AssistantStreamEvent
//   @wrapper case Done(d: String) extends AssistantStreamEvent

enum AssistantStreamEvent derives Schema:
  case ThreadCreated(thread: Thread)
  case ThreadRunCreated(run: Run)
  case ThreadRunQueued(run: Run)
  case ThreadRunInProgress(run: Run)
  case ThreadRunRequiresAction(run: Run)
  case ThreadRunCompleted(run: Run)
  case ThreadRunIncomplete(run: Run)
  case ThreadRunFailed(run: Run)
  case ThreadRunCancelling(run: Run)
  case ThreadRunCancelled(run: Run)
  case ThreadRunExpired(run: Run)
  case ThreadRunStepCreated(runStep: RunStep)
  case ThreadRunStepInProgress(runStep: RunStep)
  case ThreadRunStepDelta(runStepDelta: RunStepDelta)
  case ThreadRunStepCompleted(runStep: RunStep)
  case ThreadRunStepFailed(runStep: RunStep)
  case ThreadRunStepCancelled(runStep: RunStep)
  case ThreadRunStepExpired(runStep: RunStep)
  case ThreadMessageCreated(message: Message)
  case ThreadMessageInProgress(message: Message)
  case ThreadMessageDelta(messageDelta: MessageDelta)
  case ThreadMessageCompleted(message: Message)
  case ThreadMessageIncomplete(message: Message)
  case Error(error: String)
  case Done()
  case Unknown(event: String, data: String)
end AssistantStreamEvent

extension [A](errorOr: Either[PayloadError, A])
  def failFast: A = errorOr.fold(
    throw _,
    identity
  )

extension (s: String) def blob = Blob(s)

private def eventFromId(eventId: String, data: String): AssistantStreamEvent = eventId match
  case "thread.created"             => AssistantStreamEvent.ThreadCreated(Json.read[Thread](data.blob).failFast)
  case "thread.run.created"         => AssistantStreamEvent.ThreadRunCreated(Json.read[Run](data.blob).failFast)
  case "thread.run.queued"          => AssistantStreamEvent.ThreadRunQueued(Json.read[Run](data.blob).failFast)
  case "thread.run.in_progress"     => AssistantStreamEvent.ThreadRunInProgress(Json.read[Run](data.blob).failFast)
  case "thread.run.requires_action" => AssistantStreamEvent.ThreadRunRequiresAction(Json.read[Run](data.blob).failFast)
  case "thread.run.completed"       => AssistantStreamEvent.ThreadRunCompleted(Json.read[Run](data.blob).failFast)
  case "thread.run.incomplete"      => AssistantStreamEvent.ThreadRunIncomplete(Json.read[Run](data.blob).failFast)
  case "thread.run.failed"          => AssistantStreamEvent.ThreadRunFailed(Json.read[Run](data.blob).failFast)
  case "thread.run.cancelling"      => AssistantStreamEvent.ThreadRunCancelling(Json.read[Run](data.blob).failFast)
  case "thread.run.cancelled"       => AssistantStreamEvent.ThreadRunCancelled(Json.read[Run](data.blob).failFast)
  case "thread.run.expired"         => AssistantStreamEvent.ThreadRunExpired(Json.read[Run](data.blob).failFast)
  case "thread.run.step.created"    => AssistantStreamEvent.ThreadRunStepCreated(Json.read[RunStep](data.blob).failFast)
  case "thread.run.step.in_progress" =>
    AssistantStreamEvent.ThreadRunStepInProgress(Json.read[RunStep](data.blob).failFast)
  case "thread.run.step.delta" => AssistantStreamEvent.ThreadRunStepDelta(Json.read[RunStepDelta](data.blob).failFast)
  case "thread.run.step.completed" =>
    AssistantStreamEvent.ThreadRunStepCompleted(Json.read[RunStep](data.blob).failFast)
  case "thread.run.step.failed" => AssistantStreamEvent.ThreadRunStepFailed(Json.read[RunStep](data.blob).failFast)
  case "thread.run.step.cancelled" =>
    AssistantStreamEvent.ThreadRunStepCancelled(Json.read[RunStep](data.blob).failFast)
  case "thread.run.step.expired" => AssistantStreamEvent.ThreadRunStepExpired(Json.read[RunStep](data.blob).failFast)
  case "thread.message.created"  => AssistantStreamEvent.ThreadMessageCreated(Json.read[Message](data.blob).failFast)
  case "thread.message.in_progress" =>
    AssistantStreamEvent.ThreadMessageInProgress(Json.read[Message](data.blob).failFast)
  case "thread.message.delta" => AssistantStreamEvent.ThreadMessageDelta(Json.read[MessageDelta](data.blob).failFast)
  case "thread.message.completed" => AssistantStreamEvent.ThreadMessageCompleted(Json.read[Message](data.blob).failFast)
  case "thread.message.incomplete" =>
    AssistantStreamEvent.ThreadMessageIncomplete(Json.read[Message](data.blob).failFast)
  case "error" => AssistantStreamEvent.Error(data)
  case "done"  => AssistantStreamEvent.Done()
  case _       => AssistantStreamEvent.Unknown(eventId, data)

extension (c: RunApi)

  def streamToolOutput(
      authdClient: Client[IO],
      thread_id: String,
      run_id: String,
      tool_outputs: List[ToolOutput],
      baseUrl: String = "https://api.openai.com"
  ) =
    val enc: EntityEncoder[IO, StreamToolOutput] =
      EntityEncoder.encodeBy[IO, StreamToolOutput](("Content-Type" -> "application/json"))(scr =>
        Entity(fs2.Stream.emits[IO, Byte](smithy4s.json.Json.writeBlob(scr).toArray))
      )
    val req = Request[IO](
      Method.POST,
      Uri.unsafeFromString(baseUrl + s"/v1/threads/$thread_id/runs/$run_id/submit_tool_outputs")
    ).withEntity(
      StreamToolOutput(
        tool_outputs
      )
    )(using enc)
    // fs2.Stream.eval(IO.println(s"sending tool outputs $req")) >>
    streamAssistantEvents(authdClient, req)
  end streamToolOutput

  def streamRun(
      authdClient: Client[IO],
      thread_id: String,
      assistant_id: String,
      model: Option[String] = None,
      instructions: Option[String] = None,
      additional_instructions: Option[String] = None,
      additional_messages: Option[List[ThreadMessage]] = None,
      tools: Option[List[AssistantTool]] = None,
      metadata: Option[RunMetaData] = None,
      temperature: Option[Double] = None,
      top_p: Option[Double] = None,
      max_prompt_tokens: Option[Long] = None,
      max_completion_tokens: Option[Long] = None,
      truncation_strategy: Option[TruncationStrategy] = None,
      tool_choice: Option[ToolChoiceInRun] = None,
      parallel_tool_calls: Option[Boolean] = None,
      response_format: Option[ResponseFormat] = None,
      baseUrl: String = "https://api.openai.com"
  ): fs2.Stream[IO, AssistantStreamEvent] =
    val enc: EntityEncoder[IO, StreamThreadRunRequest] =
      EntityEncoder.encodeBy[IO, StreamThreadRunRequest](("Content-Type" -> "application/json"))(scr =>
        Entity(fs2.Stream.emits[IO, Byte](smithy4s.json.Json.writeBlob(scr).toArray))
      )
    val req = Request[IO](
      Method.POST,
      Uri.unsafeFromString(baseUrl + "/v1/threads/runs")
    ).withEntity(
      StreamThreadRunRequest(
        assistant_id = assistant_id,
        model = model,
        instructions = instructions,
        tools = tools,
        metadata = metadata,
        top_p = top_p,
        max_prompt_tokens = max_prompt_tokens,
        max_completion_tokens = max_completion_tokens,
        truncation_strategy = truncation_strategy,
        tool_choice = tool_choice,
        parallel_tool_calls = parallel_tool_calls,
        response_format = response_format
      )
    )(using enc)
    streamAssistantEvents(authdClient, req)
  end streamRun

  def createThreadRunStream(
      authdClient: Client[IO],
      assistant_id: String,
      thread: CreateThread,
      model: Option[String] = None,
      instructions: Option[String] = None,
      additional_instructions: Option[String] = None,
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
      response_format: Option[ResponseFormat] = None,
      baseUrl: String = "https://api.openai.com"
  ): fs2.Stream[IO, AssistantStreamEvent] =
    val enc: EntityEncoder[IO, StreamRunRequest] =
      EntityEncoder.encodeBy[IO, StreamRunRequest](("Content-Type" -> "application/json"))(scr =>
        Entity(fs2.Stream.emits[IO, Byte](smithy4s.json.Json.writeBlob(scr).toArray))
      )
    val req = Request[IO](
      Method.POST,
      Uri.unsafeFromString(baseUrl + "/v1/threads/runs")
    ).withEntity(
      StreamRunRequest(
        assistant_id,
        thread,
        model,
        instructions,
        additional_instructions,
        tools,
        tool_resources,
        metadata,
        temperature,
        top_p,
        max_prompt_tokens,
        max_completion_tokens,
        truncation_strategy,
        tool_choice,
        parallel_tool_calls,
        response_format
      )
    )(using enc)
    streamAssistantEvents(authdClient, req)
  end createThreadRunStream
end extension

def streamAssistantEvents(authdClient: Client[IO], req: Request[IO]) =
  authdClient
    .stream(
      req
    )
    .debug()

    // .flatMap(str =>
    //   str.bodyText.evalMap { inStr =>
    //     val strSplit = inStr.split("\n")
    //     val event = strSplit(0).drop(7)
    //     val data = strSplit(1).drop(6)
    //     // IO.println(event) >>
    //     //   IO.println(data) >>
    //     IO(eventFromId(event, data)).onError { err =>
    //       IO.println("Stream parsing failed -----") >>
    //         IO.println(event) >>
    //         IO.println(data) >>
    //         IO.raiseError(err)
    //     }
    //   }
    // )
    .flatMap(resp =>
      resp.body.through(ServerSentEvent.decoder).map {
        case ServerSentEvent(Some(data), Some(event), _, _, _) =>
          eventFromId(event, data)
        case ServerSentEvent(data, eventType, id, retry, comment) =>
          println(s"event: $eventType, data: $data, eventType: $eventType, id: $id, retry: $retry, comment: $comment")
          AssistantStreamEvent.Unknown(eventType.getOrElse(""), data.getOrElse(""))
      }
    )
end streamAssistantEvents

/** https://platform.openai.com/docs/api-reference/runs
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
      submit_tool_outputs: ToolOutputs
  ) derives Schema

  case class ToolOutputs(
      tool_calls: List[ToolCall]
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

  case class StreamRunRequest(
      assistant_id: String,
      thread: CreateThread,
      model: Option[String] = None,
      instructions: Option[String] = None,
      additional_instructions: Option[String] = None,
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
      response_format: Option[ResponseFormat] = None,
      stream: Boolean = true
  ) derives Schema

  case class StreamThreadRunRequest(
      assistant_id: String,
      model: Option[String] = None,
      instructions: Option[String] = None,
      additional_instructions: Option[String] = None,
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
      response_format: Option[ResponseFormat] = None,
      stream: Boolean = true
  ) derives Schema

  case class StreamToolOutput(
      tool_outputs: List[ToolOutput],
      stream: Boolean = true
  ) derives Schema

end RunApi
