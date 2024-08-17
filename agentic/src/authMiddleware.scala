package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import org.http4s.AuthScheme
import org.http4s.Credentials
import org.http4s.Headers
import org.http4s.client.Client
import org.http4s.headers.Authorization

def authMiddleware(tokResource: Resource[IO, String]): org.http4s.client.Middleware[IO] = (client: Client[IO]) =>
  Client { req =>
    tokResource.flatMap { tok =>
      client.run(
        req.withHeaders(
          req.headers ++ Headers(Authorization(Credentials.Token(AuthScheme.Bearer, tok)))
        )
      )
    }
  }

def assistWare: org.http4s.client.Middleware[IO] = (client: Client[IO]) =>
  Client { req =>
    client.run(
      req.withHeaders(
        req.headers ++ Headers(("OpenAI-Beta", "assistants=v2"))
      )
    )
  }
