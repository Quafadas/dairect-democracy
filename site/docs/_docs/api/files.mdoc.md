---
title: Files API
---

# Files API

Call the files API

```scala mdoc

import io.github.quafadas.dairect.*
import cats.effect.IOApp
import cats.effect.IO
import scala.annotation.experimental
import smithy4s._
import smithy4s.kinds.PolyFunction
import cats.effect.unsafe.implicits.global


val (fApi, _) = FilesApi.defaultAuthLogToFile( fs2.io.file.Path("log.txt")).allocated.Ø
val all = fApi.files().Ø
fApi.getFile(all.data.head.id).Ø
/**
 *  fApi.deleteFile(all.data.head.id).Ø
**/
```
The somewhat painful part about the files API, is that it is (quite reasonably) not JSON encoded. The file upload method accepts `multipart/form` data, therefore, we pain~fully~stakingly handcraft the request, which is found as an extension method to the fileApi trait.

We also need in scope a `Files` fs2 implicit, so here it's wrapped in `IOApp`

```scala
import io.github.quafadas.dairect.*
import cats.effect.IOApp
import cats.effect.IO
import scala.annotation.experimental
import smithy4s.Document
import smithy4s.kinds.PolyFunction
import smithy4s.deriving.{*, given}
import scala.concurrent.duration.*

import scala.concurrent.Future
import io.github.quafadas.dairect.ChatGpt.AiMessage
import smithy4s.json.Json
import smithy4s.Blob
import fs2.io.file.*
import cats.effect.ExitCode
import org.http4s.ember.client.EmberClientBuilder
import ciris.*


object FileTest extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
    val logger = fileLogger(Path("log.txt"))
    val client = EmberClientBuilder.default[IO].build.map(authMiddleware(apikey))
    val (fApi, _) = FilesApi.defaultAuthLogToFile( fs2.io.file.Path("log.txt")).allocated.Ø

    println(fApi.upload[IO](file = fs2.io.file.Path("sample-pdf-file.pdf"), client).Ø)

    IO(ExitCode.Success)
  end run

}

```