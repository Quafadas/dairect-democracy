package io.github.quafadas.dairect

import io.github.quafadas.dairect.RunStepsApi.RunStep

class RunStepTest extends ParseSuite:

  test("correct JSON  RunStep 1") {
    val jsonString =
      """{"id":"step_rE68t6nSJuXsEeaY9ntuhnjN","object":"thread.run.step","created_at":1725376135,"run_id":"run_M6cHAo1tVXcTRnqux5uJcDAB","assistant_id":"asst_PrrltDml7XgFrDQNZ7GkgSlN","thread_id":"thread_xkbYiYx1jC6siRlJEauzvB5A","type":"tool_calls","status":"failed","cancelled_at":null,"completed_at":null,"expires_at":1725376733,"failed_at":1725376136,"last_error":{"code":"server_error","message":"Sorry, something went wrong."},"step_details":{"type":"tool_calls","tool_calls":[]},"usage":{"prompt_tokens":0,"completion_tokens":16,"total_tokens":16}}"""
        .trim()

    parseCheck[RunStep](jsonString)

  }

end RunStepTest
