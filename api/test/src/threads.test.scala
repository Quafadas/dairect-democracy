package io.github.quafadas.dairect

import io.github.quafadas.dairect.ThreadApi.Thread

class ThreadTest extends ParseSuite:

  test("correct JSON thread ") {
    val jsonString = """{
  "id": "thread_abc123",
  "object": "thread",
  "created_at": 1699012949,
  "metadata": {},
  "tool_resources": {}
}""".trim()
    val md = parseCheck[Thread](jsonString)
    assertEquals(md.id, "thread_abc123")
  }

  test("correct JSON threads again ") {
    val jsonString = """{
  "id": "thread_abc123",
  "object": "thread",
  "created_at": 1699014083,
  "metadata": {},
  "tool_resources": {
    "code_interpreter": {
      "file_ids": []
    }
  }
}""".trim()
    val md = parseCheck[Thread](jsonString)
    assertEquals(md.id, "thread_abc123")
  }

end ThreadTest
