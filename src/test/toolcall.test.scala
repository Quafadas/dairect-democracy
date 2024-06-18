package io.github.quafadas.dairect

import smithy4s.deriving.{given, *}
import smithy4s.deriving.aliases.{*, given}
import cats.effect.{IO, SyncIO}
import munit.CatsEffectSuite
import io.github.quafadas.dairect.Agent.AiChoice
import io.github.quafadas.dairect.Agent.ChatGpt
import io.github.quafadas.dairect.Agent.AiMessage
import scala.language.experimental
import smithy4s.Document
import io.github.quafadas.dairect.Agent.ChatResponse
import io.github.quafadas.dairect.Agent.AiAnswer
import scala.annotation.experimental
import io.github.quafadas.dairect.Agent.FunctionCall
import io.github.quafadas.dairect.Agent.ToolCall
import io.github.quafadas.dairect.Agent.AiResponseFormat
import io.github.quafadas.dairect.Agent.AiTokenUsage
import smithy4s.deriving.API
import smithy4s.deriving.aliases.simpleRestJson
import io.github.quafadas.dairect.Agent.ChatGptConfig
import smithy.api.Http
import smithy.api.NonEmptyString

@experimental
trait FakeTool derives API:  
  def fakeFunctionName(
    a: Int,
    b: Int
  ): IO[Int] = IO(a+b)
end FakeTool

@experimental
class ToolCallSuite extends CatsEffectSuite {

  lazy val firstMessage = AiMessage.user("call a fake tool")

  lazy val fakeAi = new ChatGpt() {

    

    lazy val secondResponse : ChatResponse =  ChatResponse(
      id = "fake-id",
      created = 1,
      model = "fake-model",
      usage = AiTokenUsage(
        0, 0, 0
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

    lazy val firstResponse : ChatResponse =  ChatResponse(
      id = "fake-id",
      created = 1,
      model = "fake-model",
      usage = AiTokenUsage(
        0, 0, 0
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
        model: String,
        messages: List[AiMessage],        
        temperature: Option[Double],        
        tools: Option[Document] = None,
        responseFormat: Option[AiResponseFormat],
    ): IO[ChatResponse] = 
      if messages.last == firstMessage then        
        IO.pure(firstResponse)
      else      
        IO.pure(secondResponse )
  }
  
  lazy val fakeTool = new FakeTool {}

  lazy val fakeParams = ChatGptConfig(
    model = "fake-model",    
    temperature = Some(0.0)    
  )

  test("Tool can called by the AI") {      
        val startAgent = Agent.startAgent(fakeAi, List(firstMessage), fakeParams, API[FakeTool].liftService(fakeTool))
        // startAgent.flatMap(IO.println) >>
        assertIO(startAgent.map(_.length), 4) >>
        assertIO(startAgent.map(_.head.content), Some("call a fake tool")) >>
        assertIO(startAgent.map( l => l(1).content), None) >>
        assertIO(startAgent.map( l => l(1).tool_calls.size), 1) >>
        assertIO(startAgent.map(l => l(2).content ), Some("3")) >>        
        assertIO(startAgent.map(_.last.content), Some("Finished"))              
  }

}