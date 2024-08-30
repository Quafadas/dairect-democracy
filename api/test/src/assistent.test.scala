package io.github.quafadas.dairect

import munit.FunSuite
import smithy4s.json.Json
import io.github.quafadas.dairect.AssistantApi.Assistant
import smithy4s.Blob

extension (s: String) def blob = Blob(s)

class JsonParsingTest extends FunSuite:

  test("correct JSON is parsed to an Assistant") {
    val jsonString = """
      {
  "id": "asst_abc123",
  "object": "assistant",
  "created_at": 1698984975,
  "name": "Math Tutor",
  "description": null,
  "model": "gpt-4o",
  "instructions": "You are a personal math tutor. When asked a question, write and run Python code to answer the question.",
  "tools": [
    {
      "type": "code_interpreter"
    },
    {
      "type": "file_search",
      "file_search": {
        "max_num_results": 5,
        "ranking_options": {
          "ranker": "auto",
          "score_threshold": 0.5
        }
      }
    }
  ],
  "metadata": {},
  "top_p": 1.0,
  "temperature": 1.0,
  "response_format": "auto"
}""".trim()

    val parsedResult = Json.read[Assistant](jsonString.blob)

    println(parsedResult)

    parsedResult match
      case Right(assistant) =>
        assert(assistant.id == "asst_abc123")
        assert(assistant.tools.contains(AssistantTool.code_interpreter()))
      case Left(error) => fail(s"Parsing failed with error: $error")
    end match
  }
end JsonParsingTest
