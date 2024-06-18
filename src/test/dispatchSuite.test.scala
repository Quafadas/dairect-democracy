package io.github.quafadas.dairect

import smithy4s.deriving.{given, *}
import smithy4s.deriving.aliases.{*, given}
import cats.effect.IO
import smithy4s.json.Json
import io.github.quafadas.dairect.Agent.FunctionCall
import munit.CatsEffectSuite
import smithy4s.Document

class FunctionCallSuite extends CatsEffectSuite:

  test("simple function is correctly dispatched") {
    val fakeTool = API[FakeTool].liftService(
      new FakeTool:
        override def fakeFunctionName(
            a: Int,
            b: Int
        ): IO[Int] = IO(a + b)
    )
    val simpleTool = ioToolGen.openAiSmithyFunctionDispatch(fakeTool)

    val call = FunctionCall(
      name = "fakeFunctionName",
      description = None,
      arguments = Some("""{"a": 1, "b": 2}""")
    )    

    assertIO(
      simpleTool(call).map(Json.writeDocumentAsPrettyString),
      "3"
    )
  }

end FunctionCallSuite
