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
import fs2.text.{lines, utf8Encode}

import org.http4s.websocket.WebSocketFrame.Text

import org.http4s.Message
import fs2.text.utf8

@main def assistantTest =

  val simpleSchema = Document.obj(
    ("type", Document.fromString("object")),
    (
      "properties",
      Document.obj(
        (
          "price",
          Document.obj(
            ("type", Document.fromString("number"))
          )
        )
      )
    )
  )

  val (assistantApi, _) = AssistantApi.defaultAuthLogToFileAddHeader(fs2.io.file.Path("assistant.txt")).allocated.Ø
  val newAssist = assistantApi
    .create(
      "gpt-4o-mini",
      tools = List(
        AssistantTool.code_interpreter(),
        AssistantTool.file_search(),
        AssistantTool.function(AssistantToolFunction("testy", Some(simpleSchema)))
      )
    )
    .Ø
  println(newAssist)

  // val assist2 = assistantApi.getAssisstant(newAssist.id).Ø
  // println(assist2)

  assistantApi.deleteAssisstant(newAssist.id)

  // val newAssist = assistantApi.create("gpt-4o-mini").Ø

end assistantTest

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

@main def streamTest =
  val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
  val logger = fileLogger(Path("log.txt"))
  val client = EmberClientBuilder.default[IO].build.map(authMiddleware(apikey)).map(logger)

  val (chat, _) = ChatGpt.defaultAuthLogToFile(Path("log.txt")).allocated.Ø

  // val streamIo = chat.streamRaw(
  //   List(AiMessage.system("You are cow"), AiMessage.user("Make noise") ),
  //   authdClient = client
  // )

  // val er = streamIo.flatMap{ str =>
  //   IO.println("startin ") >>
  //   str.compile.toList.map(_.flatten)
  // }.Ø

  val streamEasy = chat.stream(
    List(AiMessage.system("You are cow"), AiMessage.user("Make noise")),
    authdClient = client
  )

  val arg = streamEasy.debug().compile.toList

  println(arg.Ø)
end streamTest

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

  val newThread = threadApi.create(
    List(
      ThreadMessage(      
        "I am cow".msg
      )
    )
  ).Ø

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

@main def MesagesTest =
  val vsApi = VectorStoreApi.defaultAuthLogToFile(fs2.io.file.Path("vectorStore.txt")).allocated.map(_._1).Ø
  val (threadApi, _) = ThreadApi.defaultAuthLogToFile(fs2.io.file.Path("vectorStore.txt")).allocated.Ø
  val (msgApi, _) = MessagesApi.defaultAuthLogToFile(fs2.io.file.Path("messages.txt")).allocated.Ø
  val (fApi, _) = FilesApi.defaultAuthLogToFile(Path("log.txt")).allocated.Ø

  // file-7PeCahfYyjto2QIxNdOK1gcZ is a png
  // println(fApi.files().Ø)

  val vs = vsApi.list().Ø.data.head.id

  val newThread = threadApi
    .create(
      List(ThreadMessage("I am cow".msg)),
      ToolResources(file_search = FileSearch(vector_store_ids = VectorStoreIds(List(vs)).some).some).some
    )
    .Ø

  // println("make new message")
  // val msg = msgApi.create(
  //   newThread.id,
  //   "I am cow".msg,
  //   None,
  //   None
  // ).Ø
  println(newThread)

  println("make new message")
  val msg2 = msgApi
    .create(
      newThread.id,
      "i am cow".msg
    )
    .Ø

  println(msg2)

  println("make new message")
  val msg = msgApi
    .create(
      newThread.id,
      MessageOnThread.StrCase("i am cow")
    )
    .Ø

  println(msg)

  println("make new message1")
  val msg1 = msgApi
    .create(
      newThread.id,
      MessageOnThread.array(
        List(
          MessageToSend.TextCase(TextToSend("I am cow2")),
          MessageToSend.Image_fileCase(ImageFile(ImageDetails("file-7PeCahfYyjto2QIxNdOK1gcZ", None))),
          MessageToSend.Image_urlCase(
            ImageUrl(
              ImageUrlDetails(
                """https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/PNG_transparency_demonstration_1.png/280px-PNG_transparency_demonstration_1.png""",
                None
              )
            )
          )
        )
      )
    )
    .Ø

  println(msg1)

  // println("make new message 2")
  // val msg1 = msgApi.create(
  //   newThread.id,

  // ).Ø

  println(msgApi.list(newThread.id).Ø)
  // println(msg1)MessageOnThread

  val deleted = threadApi.deleteThread(newThread.id).Ø

  println(deleted)

  /** vsFilesApi.create(allVs.data.head.id, allFiles.data.head.id).Ø
    *
    * vsFilesApi.delete(allVsf.data.head.id).Ø
    */

end MesagesTest

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
