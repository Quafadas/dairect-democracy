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

val syncify = new PolyFunction[IO, cats.Id]{
  def apply[A](io: IO[A]) : cats.Id[A] = io.unsafeRunSync()
}

val addDelay = new PolyFunction[IO, IO]{
  def apply[A](io: IO[A]) : IO[A] = IO.sleep(1.second) *> io
}

@experimental
@main
def Assistant() =
  val a = AssistantApi.defaultAuthLogToFileAddHeader(fs2.io.file.Path("assistant.txt")).allocated.map(_._1).unsafeRunSync()
  val a3 = a.transform(syncify)


  // a.use { assistant =>
  //   // assistant
  //   //   .create("gpt-4o-mini")
  //   //   .flatMap(IO.println) >>
  //   assistant.assistants().flatMap(IO.println) >>
  //     assistant.getAssisstant("asst_7g7FJuGyXC8mXGXUg0fTMuOa").flatMap(IO.println)

// @experimental
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
