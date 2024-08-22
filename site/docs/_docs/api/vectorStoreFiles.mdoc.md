---
title: Vector Store Files API
---

Call the assistant API

```scala mdoc

import io.github.quafadas.dairect.*
import cats.effect.IO

val (vsApi, _) = VectorStoreApi.defaultAuthLogToFile(fs2.io.file.Path("vectorStore.txt")).allocated.Ø
val (vsFilesApi, _) = VectorStoreFilesApi.defaultAuthLogToFile(fs2.io.file.Path("vectorStore.txt")).allocated.Ø

val allVs = vsApi.list().Ø
val allvsf = vsFilesApi.list(allVs.data.head.id).Ø

val file = vsFilesApi.get(allVs.data.head.id, allvsf.data.head.id).Ø

  /** vsFilesApi.create(allVs.data.head.id, allFiles.data.head.id).Ø
  *
  * vsFilesApi.delete(allVsf.data.head.id).Ø
  */

```