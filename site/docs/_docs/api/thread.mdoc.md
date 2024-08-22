---
title: Threads API
---

# Threads API

Call the thread API

```scala mdoc

import io.github.quafadas.dairect.*
import io.github.quafadas.dairect.ThreadApi.*
import io.github.quafadas.dairect.ChatGpt .*

val vsApi = VectorStoreApi.defaultAuthLogToFile(fs2.io.file.Path("thread.txt")).allocated.map(_._1).Ø
val (threadApi, _) = ThreadApi.defaultAuthLogToFile(fs2.io.file.Path("vectorStore.txt")).allocated.Ø

val newThread = threadApi.create(List(AiMessage.user("I am cow"))).Ø

println(newThread)

val vs = vsApi.list().Ø.data.head.id

val modThread = threadApi
  .modifyThread(
    newThread.id,
    ToolResources(file_search = FileSearch(vector_store_ids = VectorStoreIds(List(vs)).some).some)
  )
  .Ø

println(modThread)

val getThread = threadApi.getThread(newThread.id).Ø

println(getThread)

val deleted = threadApi.deleteThread(newThread.id).Ø

println(deleted)


```