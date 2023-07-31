package smithyOpenAI

import smithy4s._
import smithy4s.kinds.PolyFunction
import cats.effect.IO
import cats.effect.unsafe.implicits._

/**
  * If we have a service which returns an IO (i.e. plugs into the cats effect infrastructure)
  *
  * let's make it simple to drive in a repl...
  *
  */

val sync: PolyFunction[IO, cats.Id] = new PolyFunction[IO, cats.Id]{
  def apply[A](result: IO[A]): cats.Id[A] = result.unsafeRunSync()
}