# Dairect Democracy

1. A terse API for the openAI platform

2. An experiment in Agentic AI.


I wanted the API expressed in terms of `IO`. I wanted the docs in terms of not `IO`. Forgive me.

```scala
import cats.effect.IO
import cats.effect.unsafe.implicits.global

extension [A](a: IO[A]) inline def Ø = a.unsafeRunSync()
```

