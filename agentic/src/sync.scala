package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.unsafe.implicits.*

/** If we have a service which returns an IO (i.e. plugs into the cats effect infrastructure)
  *
  * let's make it simple to drive in a repl...
  */

// val sync: PolyFunction[IO, cats.Id] = new PolyFunction[IO, cats.Id]:
//   def apply[A](result: IO[A]): cats.Id[A] = result.unsafeRunSync()

extension [A](a: IO[A])
  inline def Ø: A = a.unsafeRunSync()
  inline def streamFs2: fs2.Stream[IO, A] = fs2.Stream.eval(a)
end extension

extension [A](a: A) inline def some: Option[A] = Some(a)
