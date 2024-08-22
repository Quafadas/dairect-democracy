---
title: Assistant API
---

# Assistant API

Call the assistant API

```scala mdoc

import io.github.quafadas.dairect.*
import cats.effect.IO

val (assistantApi,_) = AssistantApi.defaultAuthLogToFileAddHeader(fs2.io.file.Path("assistant.txt")).allocated.Ø
val allAssisants = assistantApi.list().Ø
val first = assistantApi.getAssisstant(allAssisants.data.head.id).Ø

/**  Other API calls.

assistantApi.deleteAssisstant(first.data.id).Ø
assistantApi.create("gpt-4o-mini").Ø

**/


```