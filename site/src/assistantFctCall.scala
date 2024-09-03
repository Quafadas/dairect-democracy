package io.github.quafadas.dairect

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.unsafe.implicits.global
import ciris.*
import fs2.io.file.*
import io.github.quafadas.dairect.ChatGpt.AiMessage
import io.github.quafadas.dairect.RunApi.CreateThread
import io.github.quafadas.dairect.VectorStoreFilesApi.ChunkingStrategy
import io.github.quafadas.dairect.VectorStoreFilesApi.StaticChunkingStrategy
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import smithy4s.Document
import smithy4s.deriving.*
import smithy4s.json.Json

@main def assistantFctCall =

  val osTools = API[OsTool].liftService(osImpl)

  val schema = osTools.toJsonSchema
  val osDispatch = osTools.dispatcher

  println(osTools.assistantTool)

end assistantFctCall
