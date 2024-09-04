# Dairect Democracy

1. A terse API for the openAI platform in `cats.effect.IO`. This is the API module.

    - Cross compiled for JVM, JS and native.

2. An experiment in Agentic AI. This is the agentic module

    - JVM only (it relies on Amazon's smithy tooling - no cross compiling)
    - Any smithy4s `@simpleRestJson` service becomes a tool that an LLM can call. It's easy to write new ones with the incredible [smithy deriving](https://github.com/neandertech/smithy4s-deriving)

3. Experiments - this is the site module, it is not published, but there's plenty of food for thought in there

This project is based on the (i've said it before incredible) smithy deriving. You'll need the experimental compiler flag on to use it.


```
def scalacOptions = Seq("-experimental")
```

## Scala-cli

```
//> using dep io.github.quafadas::dairect-democracy::{{projectVersion}}
```

## Mill
```
ivy"io.github.quafadas::vecxt::{{projectVersion}}"
```


I wanted the API expressed in terms of `IO`. I wanted the docs in terms of not `IO`. Forgive me.

```scala
import cats.effect.IO
import cats.effect.unsafe.implicits.global

extension [A](a: IO[A]) inline def Ø = a.unsafeRunSync()
```