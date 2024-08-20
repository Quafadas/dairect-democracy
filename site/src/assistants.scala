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

@main def testy = 
  val (assistantApi,_) = AssistantApi.defaultAuthLogToFileAddHeader(fs2.io.file.Path("assistant.txt")).allocated.Ø
  val allAssisants = assistantApi.list().Ø
  val first = assistantApi.getAssisstant(allAssisants.data.head.id).Ø

  println(allAssisants)
  println(first)


 
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
