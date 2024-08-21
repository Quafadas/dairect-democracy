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

@main def testy =
  val logFile = fs2.io.file.Path("easychat.txt")
  val chatGpt = ChatGpt.defaultAuthLogToFile(logFile).allocated.map(_._1).Ø
  val osTools = API[OsTool].liftService(osImpl)

  val schema = ioToolGen.toJsonSchema(osTools)
  val osDispatch = ioToolGen.openAiSmithyFunctionDispatch(osTools)
  
  val resp = chatGpt.chat(
    List(AiMessage.system("You are a helpful assistant"), AiMessage.user("Create a temporary directory")),
    tools = schema.some
  ).Ø

  val toolCall = resp.choices.head

  val fctCall = toolCall.message.tool_calls.get.head
  val out = osDispatch(fctCall.function).Ø

  val tooloutcome = AiMessage.tool( tool_call_id = fctCall.id, content = Json.writeDocumentAsPrettyString(out) )

  chatGpt.chat(
    List(AiMessage.system("You are a helpful assistant"), AiMessage.user("Create a temporary directory"), toolCall.toMessage.head, tooloutcome),
    tools = schema.some
  ).Ø

end testy

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
