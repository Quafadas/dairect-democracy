package io.github.quafadas.dairect

import io.github.quafadas.dairect.RunStepsApi.RunStepDelta


class RunStepDeltaTest extends ParseSuite:

  test("correct JSON is parsed to an Message Delta") {
    val jsonString = """{
  "id": "step_123",
  "object": "thread.run.step.delta",
  "delta": {
    "step_details": {
      "type": "tool_calls",
      "tool_calls": [
        {
          "index": 0,
          "id": "call_123",
          "type": "code_interpreter",
          "code_interpreter": { "input": "", "outputs": [] }
        }
      ]
    }
  }
}""".trim()

    val md = parseCheck[RunStepDelta](jsonString)

    assertEquals(md.id, "step_123")
    
  }
end RunStepDeltaTest
