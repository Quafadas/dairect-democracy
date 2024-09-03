package io.github.quafadas.dairect

import munit.FunSuite
import smithy4s.json.Json
import io.github.quafadas.dairect.AssistantApi.Assistant
import smithy4s.Blob
import io.github.quafadas.dairect.MessagesApi.MessageDelta

class MessageDeltaTest extends ParseSuite:

  test("correct JSON is parsed to an Message Delta") {
    val jsonString = """{
  "id": "msg_123",
  "object": "thread.message.delta",
  "delta": {
    "content": [
      {
        "index": 0,
        "type": "text",
        "text": { "value": "Hello", "annotations": [] }
      }
    ]
  }
}""".trim()

    val md = parseCheck[MessageDelta](jsonString)

    assertEquals(md.id, "msg_123")
  }

  test("without annotations") {
    val str =
      """{"id":"msg_Wtm0AYTAVAYz2rg8N5TiuU8J","object":"thread.message.delta","delta":{"content":[{"index":0,"type":"text","text":{"value":" Scala"}}]}}"""
    val md = parseCheck[MessageDelta](str)
    assertEquals(md.id, "msg_Wtm0AYTAVAYz2rg8N5TiuU8J")
  }

end MessageDeltaTest
