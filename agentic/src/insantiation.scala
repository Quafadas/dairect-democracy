package io.github.quafadas.dairect

import ciris._
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.all.uri
import org.http4s.client.middleware.{RequestLogger, ResponseLogger}
import cats.effect.kernel.Resource
import cats.effect.IO



extension (c: ChatGpt.type)
  def defaultAuthLogToFile(
      logPath: fs2.io.file.Path,
      provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  ): Resource[IO, ChatGpt] =
    val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
    val logger = fileLogger(logPath)
    for
      _ <- makeLogFile(logPath).toResource
      client <- provided
      authdClient = authMiddleware(apikey)(logger(client))
      chatGpt <- ChatGpt.apply((authdClient), uri"https://api.openai.com/")
    yield chatGpt
    end for
  end defaultAuthLogToFile
end extension

