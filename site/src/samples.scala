package io.github.quafadas.dairect

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.unsafe.implicits.global
import ciris.*
import fs2.io.file.*
import cats.effect.std.Queue
import io.github.quafadas.dairect.ChatGpt.AiMessage
import io.github.quafadas.dairect.RunApi.CreateThread
import io.github.quafadas.dairect.VectorStoreFilesApi.ChunkingStrategy
import io.github.quafadas.dairect.VectorStoreFilesApi.StaticChunkingStrategy
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import smithy4s.Document
import smithy4s.deriving.*
import smithy4s.json.Json
import cats.syntax.all.toTraverseOps
import cats.effect.kernel.Ref
import io.github.quafadas.dairect.RunApi.Run
import fs2.concurrent.SignallingRef
import io.github.quafadas.dairect.RunApi.ToolOutput
import smithy4s.deriving.given

lazy val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
lazy val logger = fileLogger(Path("log.txt"))

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
  val schema = osTools.toJsonSchema
  val osDispatch = osTools.dispatcher

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
    val client = EmberClientBuilder.default[IO].build.map(authMiddleware(apikey)).map(logger).allocated.map(_._1).Ø

    val (fApi, _) = FilesApi.defaultAuthLogToFile(Path("log.txt")).allocated.Ø

    println(fApi.upload[IO](file = fs2.io.file.Path("C:\\temp\\sample-pdf-file.pdf"), authdClient = client).Ø)

    val all = fApi.files().Ø
    println(all)

    // println(fApi.deleteFile(all.data.head.id).Ø)

    IO(ExitCode.Success)
  end run
end FileTest

@main def streamTest =
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

  val s = client.use { c =>
    IO(
      chat.stream(
        messages = List(AiMessage.system("You are cow"), AiMessage.user("Make noise")),
        authdClient = c
      )
    )
  }

  s.streamFs2.flatten

  val arg = s.streamFs2.flatten.debug().compile.toList

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

  val newThread = threadApi
    .create(
      List(
        ThreadMessage(
          "I am cow".msg
        )
      )
    )
    .Ø

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
      tool_resources =
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

val ioToolGen = new SmithyOpenAIUtil[IO]

@main def uploadFiles =
  val OpenAiPlatform(
    chatGpt,
    assistantApi,
    filesApi,
    vectorStoreApi,
    vectorStoreFilesApi,
    runApi,
    runStepsApi,
    threadApi,
    messageApi,
    httpClient
  ) = OpenAiPlatform.defaultAuthLogToFile().allocated.map(_._1).Ø

  val osTools = API[OsTool].liftService(osImpl)
  val vs = vectorStoreApi.create("Laminar Docs".some).Ø

  def uploadFileAddToVectorStore(file: fs2.io.file.Path, httpClient: Client[IO]) =
    IO.println("uploading file") >>
      filesApi.upload[IO](file, httpClient).flatMap { f =>
        vectorStoreFilesApi
          .create(vs.id, f.id, ChunkingStrategy.static(static = StaticChunkingStrategy(4096, 500)).some) >>
          IO.println(s"uploaded ${file} to vector store")
      }

  // read all files in resource directory with fs2
  fs2.io.file
    .Files[IO]
    .list(fs2.io.file.Path("C:\\temp\\dairect-democracy\\site\\resources"))
    .take(2)
    .foreach(file => uploadFileAddToVectorStore(file, httpClient))
    .compile
    .drain
    .Ø

  val assistant = assistantApi.create("gpt-4o").Ø

  // val thisRun = Ref.of[IO, Option[Run]](None).Ø
  val continue = SignallingRef.of[IO, ContinueFold](ContinueFold.Continue).Ø
  val interrupter = continue.map(_ == ContinueFold.Stop)

  val thread: fs2.Stream[IO, AssistantStreamEvent] = runApi
    .createThreadRunStream(
      httpClient,
      assistant.id,
      thread = CreateThread(
        List(
          ThreadMessage(
            """ Create a temporary directory.
            | Write a very short summary (in markdown format) of the key points of the scala laminar UI framework to a temporary file in the directory you created.
            |
            | Tell me the location of the temporary file""".msg
          )
        )
      ),
      tool_resources =
        ToolResources(file_search = FileSearch(vector_store_ids = VectorStoreIds(List(vs.id)).some).some).some,
      tools = Some(
        osTools.assistantTools :+ AssistantTool.file_search()
      )
    )

  // thread.flatMap{event => fs2.Stream.eval(queue.offer(event))}

  val q = Queue.unbounded[IO, AssistantStreamEvent].Ø

  /** @param events
    * @return
    */
  def processStreamEvents(
      queue: Queue[IO, AssistantStreamEvent]
  ): fs2.Stream[IO, Unit] = {
    val dispatcher = ioToolGen.openAiSmithyFunctionDispatch(osTools)
    fs2.Stream.fromQueueUnterminated(queue).collect {

      case AssistantStreamEvent.ThreadMessageCompleted(msg) => IO.println(msg).streamFs2
      case AssistantStreamEvent.ThreadRunRequiresAction(run) =>
        val toolCall = run.required_action.get
        val dispatch = toolCall.submit_tool_outputs.tool_calls.traverse { fct =>
          dispatcher(fct.function).map { out =>
            ToolOutput(fct.id, Json.writePrettyString(out))
          }
        }
        dispatch
          .map { toSend =>
            runApi
              .streamToolOutput(
                httpClient,
                run.thread_id,
                run.id,
                toSend
              )
              .evalMap(queue.offer)
          }
          .streamFs2
          .flatten

      case AssistantStreamEvent.ThreadRunCompleted(run) =>
        (IO.println("set stop condition") >>
          continue.update(_ => ContinueFold.Stop)).streamFs2

      case AssistantStreamEvent.Done() =>
        IO.println("Stream finished").streamFs2
    }
  }.flatten

  val run = thread.flatMap(a => q.offer(a).streamFs2).compile.drain

  val processEvents = processStreamEvents(
    q
  )
    .debug()
    .interruptWhen(interrupter)
    .compile
    .drain

  (run >> processEvents).Ø

  // thread.debug().compile.drain.Ø

  // println(thread)

end uploadFiles
