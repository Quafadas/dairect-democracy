package io.github.quafadas.dairect

import org.http4s.client.Client
import cats.effect.IO
import io.github.quafadas.dairect.RunApi.TruncationStrategy
import io.github.quafadas.dairect.RunApi.ToolChoiceInRun
import io.github.quafadas.dairect.RunApi.StreamThreadRunRequest
import org.http4s.EntityEncoder
import smithy4s.kinds.FunctorAlgebra
import smithy4s.Document
import cats.MonadThrow
import smithy4s.Service
import cats.effect.std.Queue
import cats.syntax.all.*
import org.http4s.Request
import org.http4s.Method
import org.http4s.Uri
import org.http4s.Entity
import io.github.quafadas.dairect.RunApi.ToolOutput
import smithy4s.json.Json
import smithy4s.deriving.given_Schema_Document
import fs2.concurrent.SignallingRef
import cats.effect.*
import io.github.quafadas.dairect.RunApi.RunStatus
import fs2.concurrent.Channel

extension (r: RunApi)
  def streamToolRun[Alg[_[_, _, _, _, _]], F[_]](
      authdClient: Client[IO],
      thread_id: String,
      assistant_id: String,
      tools: FunctorAlgebra[Alg, IO],
      model: Option[String] = None,
      instructions: Option[String] = None,
      additional_instructions: Option[String] = None,
      additional_messages: Option[List[ThreadMessage]] = None,
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
  )(using S: Service[Alg]): fs2.Stream[IO, AssistantStreamEvent] =

    val enc: EntityEncoder[IO, StreamThreadRunRequest] =
      EntityEncoder.encodeBy[IO, StreamThreadRunRequest](("Content-Type" -> "application/json"))(scr =>
        Entity(fs2.Stream.emits[IO, Byte](smithy4s.json.Json.writeBlob(scr).toArray))
      )

    // val dispatch = alg.dispatcher

    def processStreamEvents(
        incoming: fs2.Stream[IO, AssistantStreamEvent],
        queue: Queue[IO, AssistantStreamEvent],
        continue: SignallingRef[IO, ContinueFold],
        dispatcher: FunctionCall => IO[Document]
    ) =
      // .debug()
      incoming.collect {
        case AssistantStreamEvent.ThreadRunRequiresAction(run) =>
          println("Requires action evaluating")
          val toolCall = run.required_action.get
          val dispatch = toolCall.submit_tool_outputs.tool_calls.traverse { fct =>
            dispatcher(fct.function).map { out =>
              ToolOutput(fct.id, Json.writePrettyString(out))
            }
          }
          val newStream =
            fs2.Stream.eval(
              IO.println("dispatching tool calls") >>
                dispatch
                  .map { toSend =>
                    r.streamToolOutput(
                      authdClient,
                      run.thread_id,
                      run.id,
                      toSend
                    ).evalMap(queue.offer)
                  }
            )

          newStream.flatten

        case AssistantStreamEvent.ThreadRunCompleted(run) =>
          fs2.Stream.eval(
            if run.status == RunStatus.completed ||
              run.status == RunStatus.failed ||
              run.status == RunStatus.cancelling ||
              run.status == RunStatus.expired ||
              run.status == RunStatus.cancelled
            then (continue.set(ContinueFold.Stop))
            else IO.unit
          )
      }.flatten

    val channel = Channel

    val qIo = Queue.unbounded[IO, AssistantStreamEvent].toResource
    val continue = SignallingRef.of[IO, ContinueFold](ContinueFold.Continue).toResource
    val queue_interrupt = qIo.both(continue)

    val req = Request[IO](
      Method.POST,
      Uri.unsafeFromString(baseUrl + s"/v1/threads/$thread_id/runs")
    ).withEntity(
      StreamThreadRunRequest(
        assistant_id = assistant_id,
        model = model,
        instructions = instructions,
        tools = tools.assistantTools.some,
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

    val dispacher = tools.dispatcher

    val out = queue_interrupt.flatMap { case (q, interrupt) =>
      val emitted = fs2.Stream
        .fromQueueUnterminated(q)

      val seed = streamAssistantEvents(authdClient, req)
        .evalTap(q.offer)
        .compile
        .drain
        .background

      val queued = processStreamEvents(emitted, q, interrupt, dispacher).compile.drain.background

      val queueEvents = emitted
        .debug()
        .interruptWhen(interrupt.map(_ == ContinueFold.Stop))

      seed *> queued *> Resource.eval(IO(queueEvents))
    }

    fs2.Stream.resource(out).flatten

    // fs2.Stream.eval(out.map(_._2)).flatten

    // val startStreamSendToolCalls = typedStream.both(queue_interrupt).flatMap { case (_, (q, interrupt)) =>
    //   processStreamEvents(q, interrupt, dispacher).compile.drain
    // }

    // fs2.Stream
    //   .eval(
    //     startStreamSendToolCalls
    //   )
    //   .flatMap { q =>
    //     fs2.Stream.fromQueueUnterminated(q).interruptWhen(interrupter)
    //   }

  end streamToolRun
end extension
