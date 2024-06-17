package io.github.quafadas.dairect

import cats.effect.{IO, SyncIO}
import munit.CatsEffectSuite

class ToolCallSuite extends CatsEffectSuite {

  val fakeAi = new ChatGpt() {
    override def chat(
        model: String,
        messages: List[AiMessage],
        // responseFormat: AiResponseFormat,
        temperature: Option[Double],
        // functions: Option[List[ChatCompletionFunctions]] = None
        tools: Option[Document] = None
    ): IO[ChatResponse] = IO.pure(ChatGptResponse(List(ChatGptResponseChoice("Hello", None))))
  }

  test("tests can return IO[Unit] with assertions expressed via a map") {
        IO(42).map(it => assertEquals(it, 42))
  }

}