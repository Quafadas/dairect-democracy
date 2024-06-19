package io.github.quafadas.dairect

import smithy4s.deriving.API
import cats.effect.IO
import smithy4s.json.Json

class SchemaGenSuite extends munit.FunSuite:

  test("simple schema is generated") {
    val fakeTool = API[FakeTool].liftService(new FakeTool {})
    val simpleTool = ioToolGen.toJsonSchema(fakeTool)
    assertEquals(
      Json.writeDocumentAsPrettyString(simpleTool),
      """[
  {
    "type": "function",
    "function": {
      "name": "fakeFunctionName",
      "description": "fakeFunctionName",
      "parameters": {
        "type": "object",
        "required": [
          "a",
          "b"
        ],
        "properties": {
          "a": {
            "type": "number"
          },
          "b": {
            "type": "number"
          }
        }
      }
    }
  }
]"""
    )

  }

end SchemaGenSuite
