package io.github.quafadas.dairect

import org.http4s.client.Client
import cats.effect.kernel.Resource
import cats.effect.IO
import org.http4s.Headers
import org.http4s.headers.Authorization
import org.http4s.Credentials
import org.http4s.AuthScheme

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
