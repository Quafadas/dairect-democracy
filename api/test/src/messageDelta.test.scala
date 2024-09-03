package io.github.quafadas.dairect

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

  test("with annotation") {
    val str =
      """{"id":"msg_AO48GqzL1mll6R44grLhgERn","object":"thread.message.delta","delta":{"content":[{"index":0,"type":"text","text":{"value":"?4:2�source?","annotations":[{"index":0,"type":"file_citation","text":"?4:2�source?","start_index":924,"end_index":936,"file_citation":{"file_id":"file-f8HFn9QMFkele5JHegNlWeut","quote":""}}]}}]}} """
    parseCheck[MessageDelta](str)

  }

end MessageDeltaTest
