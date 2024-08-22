---
title: Vector Store API
---

Call the assistant API

```scala mdoc

import io.github.quafadas.dairect.*
import cats.effect.IO
import smithy4s._
import smithy4s.kinds.PolyFunction

val (vsApi,_) = VectorStoreApi.defaultAuthLogToFile(fs2.io.file.Path("vectorStore.txt")).allocated.Ø

val allVs = vsApi.list().Ø

/** Other API calls.
 *
  val newVs = vsApi.create().Ø
  vsApi.get(allVs.data.head.id).Ø
  vsApi.delete(allVs.data.head.id).Ø
**/

```