package io.github.quafadas.dairect

import munit.FunSuite
import smithy4s.json.Json
import io.github.quafadas.dairect.AssistantApi.Assistant
import smithy4s.Blob
import io.github.quafadas.dairect.MessagesApi.Message
import io.github.quafadas.dairect.MessagesApi.MessageList

class MessageTest extends ParseSuite:

  test("correct JSON is parsed to an Message") {
    val jsonString = """{
  "id": "msg_abc123",
  "object": "thread.message",
  "created_at": 1713226573,
  "assistant_id": null,
  "thread_id": "thread_abc123",
  "run_id": null,
  "role": "user",
  "content": [
    {
      "type": "text",
      "text": {
        "value": "How does AI work? Explain it in simple terms.",
        "annotations": []
      }
    }
  ],
  "attachments": [],
  "metadata": {}
}""".trim()

    val md = parseCheck[Message](jsonString)

    assertEquals(md.id, "msg_abc123")
  }

  test("correct JSON is parsed to an Message list") {
    val jsonString = """{
  "object": "list",
  "data": [
    {
      "id": "msg_abc123",
      "object": "thread.message",
      "created_at": 1699016383,
      "assistant_id": null,
      "thread_id": "thread_abc123",
      "run_id": null,
      "role": "user",
      "content": [
        {
          "type": "text",
          "text": {
            "value": "How does AI work? Explain it in simple terms.",
            "annotations": []
          }
        }
      ],
      "attachments": [],
      "metadata": {}
    },
    {
      "id": "msg_abc456",
      "object": "thread.message",
      "created_at": 1699016383,
      "assistant_id": null,
      "thread_id": "thread_abc123",
      "run_id": null,
      "role": "user",
      "content": [
        {
          "type": "text",
          "text": {
            "value": "Hello, what is AI?",
            "annotations": []
          }
        }
      ],
      "attachments": [],
      "metadata": {}
    }
  ],
  "first_id": "msg_abc123",
  "last_id": "msg_abc456",
  "has_more": false
}""".trim()

    parseCheck[MessageList](jsonString)

  }

end MessageTest
