package io.github.quafadas.dairect

import io.github.quafadas.dairect.VectorStoreApi.VectorStore
import io.github.quafadas.dairect.VectorStoreApi.VectorStoreList

class VectorStoreTest extends ParseSuite:

  test("correct JSON ") {
    val jsonString = """{
  "id": "vs_abc123",
  "object": "vector_store",
  "created_at": 1699061776,
  "name": "Support FAQ",
  "bytes": 139920,
  "file_counts": {
    "in_progress": 0,
    "completed": 3,
    "failed": 0,
    "cancelled": 0,
    "total": 3
  }
}""".trim()
    val md = parseCheck[VectorStore](jsonString)
    assertEquals(md.id, "vs_abc123")
  }

  test("vs list") {
    val jsonString = """{
  "object": "list",
  "data": [
    {
      "id": "vs_abc123",
      "object": "vector_store",
      "created_at": 1699061776,
      "name": "Support FAQ",
      "bytes": 139920,
      "file_counts": {
        "in_progress": 0,
        "completed": 3,
        "failed": 0,
        "cancelled": 0,
        "total": 3
      }
    },
    {
      "id": "vs_abc456",
      "object": "vector_store",
      "created_at": 1699061776,
      "name": "Support FAQ v2",
      "bytes": 139920,
      "file_counts": {
        "in_progress": 0,
        "completed": 3,
        "failed": 0,
        "cancelled": 0,
        "total": 3
      }
    }
  ],
  "first_id": "vs_abc123",
  "last_id": "vs_abc456",
  "has_more": false
}""".trim()
    parseCheck[VectorStoreList](jsonString)
  }

end VectorStoreTest
