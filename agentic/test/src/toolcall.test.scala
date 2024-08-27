package io.github.quafadas.dairect

import cats.effect.IO
import munit.CatsEffectSuite
import smithy4s.Document
import smithy4s.deriving.{*, given}

import scala.annotation.experimental
import scala.language.experimental

import ChatGpt.AiChoice
import ChatGpt.AiMessage
import ChatGpt.ChatResponse
import ChatGpt.AiAnswer
import ChatGpt.AiTokenUsage
import ChatGpt.ChatGptConfig

@experimental
trait FakeTool derives API:
  def fakeFunctionName(
      a: Int,
      b: Int
  ): IO[Int] = IO(a + b)
end FakeTool

@experimental
class ToolCallSuite extends CatsEffectSuite:

  test("Tool can called by the AI") {
    val startAgent = Agent.startAgent(fakeAi, List(firstMessage), fakeParams, API[FakeTool].liftService(fakeTool))
    // startAgent.flatMap(IO.println) >>
    assertIO(startAgent.map(_.length), 4) >>
      assertIO(startAgent.map(_.head.content), Some("call a fake tool")) >>
      assertIO(startAgent.map(l => l(1).content), None) >>
      assertIO(startAgent.map(l => l(1).tool_calls.size), 1) >>
      assertIO(startAgent.map(l => l(2).content), Some("3")) >>
      assertIO(startAgent.map(_.last.content), Some("Finished"))
  }

  lazy val firstMessage = AiMessage.user("call a fake tool")

  lazy val fakeAi = new ChatGpt():

    lazy val secondResponse: ChatResponse = ChatResponse(
      id = "fake-id",
      created = 1,
      model = "fake-model",
      usage = AiTokenUsage(
        0,
        0,
        0
      ),
      choices = List(
        AiChoice(
          message = AiAnswer(
            role = "assistant",
            content = Some("Finished"),
            tool_calls = None
          ),
          finish_reason = Some("stop")
        )
      )
    )

    lazy val firstResponse: ChatResponse = ChatResponse(
      id = "fake-id",
      created = 1,
      model = "fake-model",
      usage = AiTokenUsage(
        0,
        0,
        0
      ),
      choices = List(
        AiChoice(
          message = AiAnswer(
            role = "assistant",
            content = None,
            tool_calls = Some(
              List(
                ToolCall(
                  id = "fake-tool-id",
                  function = FunctionCall(
                    name = "fakeFunctionName",
                    description = None,
                    arguments = Some("""{"a" : 1, "b" : 2}""")
                  )
                )
              )
            )
          ),
          finish_reason = Some("tool_calls")
        )
      )
    )

    override def chat(        
        messages: List[AiMessage],
        model: String,
        temperature: Option[Double],
        tools: Option[Document] = None,
        responseFormat: Option[AiResponseFormat]
    ): IO[ChatResponse] =
      if messages.last == firstMessage then IO.pure(firstResponse)
      else IO.pure(secondResponse)

  lazy val fakeTool = new FakeTool {}

  lazy val fakeParams = ChatGptConfig(
    model = "fake-model",
    temperature = Some(0.0)
  )

end ToolCallSuite
