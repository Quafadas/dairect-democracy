package io.github.quafadas.dairect

import io.github.quafadas.dairect.VectorStoreFilesApi.VectorStoreFileList
import io.github.quafadas.dairect.VectorStoreFilesApi.VectorStoreFile

class VectorStoreFileTest extends ParseSuite:

  test("correct JSON vector store file") {
    val jsonString = """{
  "id": "file-abc123",
  "object": "vector_store.file",
  "created_at": 1699061776,
  "usage_bytes": 1234,
  "vector_store_id": "vs_abcd",
  "status": "completed",
  "last_error": null
}""".trim()
    val md = parseCheck[VectorStoreFile](jsonString)
    assertEquals(md.id, "file-abc123")
  }

  test("vs list") {
    val jsonString = """{
  "object": "list",
  "data": [
    {
      "id": "file-abc123",
      "object": "vector_store.file",
      "created_at": 1699061776,
      "vector_store_id": "vs_abc123"
    },
    {
      "id": "file-abc456",
      "object": "vector_store.file",
      "created_at": 1699061776,
      "vector_store_id": "vs_abc123"
    }
  ],
  "first_id": "file-abc123",
  "last_id": "file-abc456",
  "has_more": false
}""".trim()
    parseCheck[VectorStoreFileList](jsonString)
  }

end VectorStoreFileTest
