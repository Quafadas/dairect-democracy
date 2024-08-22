package io.github.quafadas.dairect

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
import io.github.quafadas.dairect.ThreadApi.ToolResources
import io.github.quafadas.dairect.ThreadApi.FileSearch

@main def testy =
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

  println(
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
  )

end testy

object FileTest extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
    val logger = fileLogger(Path("log.txt"))
    val client = EmberClientBuilder.default[IO].build.map(authMiddleware(apikey)).map(logger)

    val (fApi, _) = FilesApi.defaultAuthLogToFile(Path("log.txt")).allocated.Ø

    println(fApi.upload[IO](file = fs2.io.file.Path("C:\\temp\\sample-pdf-file.pdf"), authdClient = client).Ø)

    val all = fApi.files().Ø
    println(all)

    // println(fApi.deleteFile(all.data.head.id).Ø)

    IO(ExitCode.Success)
  end run
end FileTest

@main def vsFilesTest =

  val (vsApi, _) = VectorStoreApi.defaultAuthLogToFile(fs2.io.file.Path("vectorStore.txt")).allocated.Ø
  val (vsFilesApi, _) = VectorStoreFilesApi.defaultAuthLogToFile(fs2.io.file.Path("vectorStore.txt")).allocated.Ø

  val allVs = vsApi.list().Ø
  val allvsf = vsFilesApi.list(allVs.data.head.id).Ø

  val file = vsFilesApi.get(allVs.data.head.id, allvsf.data.head.id).Ø

  /** vsFilesApi.create(allVs.data.head.id, allFiles.data.head.id).Ø
    *
    * vsFilesApi.delete(allVsf.data.head.id).Ø
    */

end vsFilesTest

@main def ThreadTest =
  val vsApi = VectorStoreApi.defaultAuthLogToFile(fs2.io.file.Path("vectorStore.txt")).allocated.map(_._1).Ø
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

  /** vsFilesApi.create(allVs.data.head.id, allFiles.data.head.id).Ø
    *
    * vsFilesApi.delete(allVsf.data.head.id).Ø
    */

end ThreadTest

// object Assistant extends IOApp.Simple:

//   def run: IO[Unit] =
//     val a = AssistantApi.defaultAuthLogToFileAddHeader(fs2.io.file.Path("assistant.txt"))
//     a.use { assistant =>
//       // assistant
//       //   .create("gpt-4o-mini")
//       //   .flatMap(IO.println) >>
//       assistant.assistants().flatMap(IO.println) >>
//         assistant.getAssisstant("asst_7g7FJuGyXC8mXGXUg0fTMuOa").flatMap(IO.println)
//     }

//   end run

// end Assistant
