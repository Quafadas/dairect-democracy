---
title: Tools
---

# Make AI do OS operations

To enable business processes, we'll often need OS operations. To enable this, we can knock up a tool, which can do that. There are serious security implications. You should probably do this sort of thing in a sandbox.

Here's a simple example of an OS tool. Note how simple this is to create.

```scala sc:nocompile
import smithy4s.*
import smithy4s.deriving.{*, given}

import cats.effect.IO
import cats.effect.std.Console

@hints(smithy.api.Documentation("Local file and os operations"))
/** Local file and os operations
  */
trait OsTool derives API:
  /** Creates a temporary directory on the local file system.
    */
  def makeTempDir(dirPrefix: String): IO[String] =
    IO.println("Creating a temporary directory") >>
      IO.blocking {
        val outDir = os.temp.dir(deleteOnExit = false, prefix = dirPrefix).toString
        outDir.toString
      }

  def createOrOverwriteFileInDir(dir: String, fileName: String, contents: Option[String]): IO[String] =
    IO.println(s"Creating a file in $dir") >>
      IO.blocking {
        val filePath = os.Path(dir) / fileName
        os.write.over(filePath, contents.getOrElse(""))
        filePath.toString
      }

  def askForHelp(question: String): IO[String] =
    for
      _ <- Console[IO].println(s"I need guidance with: $question")
      n <- Console[IO].readLine
    yield n

end OsTool

```

The below mechanism, demonstrates how one may supply that tool to the AI, and dispatch an arbirary function call.

```scala mdoc sc:nocompile
import io.github.quafadas.dairect.*
import cats.effect.IOApp
import cats.effect.IO
import scala.annotation.experimental
import smithy4s.Document
import smithy4s.kinds.PolyFunction
import smithy4s.deriving.{*, given}
import scala.concurrent.duration.*

import cats.effect.unsafe.implicits.global
import scala.concurrent.Future
import io.github.quafadas.dairect.ChatGpt.AiMessage
import smithy4s.json.Json
import smithy4s.Blob
import fs2.io.file.*
import cats.effect.ExitCode
import org.http4s.ember.client.EmberClientBuilder
import ciris.*

val osImpl = new OsTool() {}

val logFile = fs2.io.file.Path("easychat.txt")
val chatGpt = ChatGpt.defaultAuthLogToFile(logFile).allocated.map(_._1).Ø
val osTools = API[OsTool].liftService(osImpl)

val schema = ioToolGen.toJsonSchema(osTools)
val osDispatch = ioToolGen.openAiSmithyFunctionDispatch(osTools)

val resp = chatGpt
  .chat(
    List(AiMessage.system("You are a helpful assistant"), AiMessage.user("Create a temporary directory")),
    tools = schema.some
  )
  .Ø

val toolCall = resp.choices.head

val fctCall = toolCall.message.tool_calls.get.head
val out = osDispatch(fctCall.function).Ø

val tooloutcome = AiMessage.tool(tool_call_id = fctCall.id, content = Json.writeDocumentAsPrettyString(out))

chatGpt
  .chat(
    List(
      AiMessage.system("You are a helpful assistant"),
      AiMessage.user("Create a temporary directory"),
      toolCall.toMessage.head,
      tooloutcome
    ),
    tools = schema.some
  )
  .Ø


```