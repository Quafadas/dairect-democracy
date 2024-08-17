package io.github.quafadas.dairect

import smithy4s.deriving.API
import cats.effect.IO
import smithy4s.json.Json
import ChatGpt.FunctionCall
import munit.CatsEffectSuite
import smithy4s.Document

class FunctionCallSuite extends CatsEffectSuite:

  test("simple function is correctly dispatched") {
    val fakeTool = API[FakeTool].liftService(
      new FakeTool {}
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
