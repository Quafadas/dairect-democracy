---
title: Basic Chat
---

# Basic Chat

A simple chat application that uses the chat API to chat with an assistant.

```scala mdoc

import io.github.quafadas.dairect.*
import io.github.quafadas.dairect.ChatGpt.*
import cats.effect.IO

val logFile = fs2.io.file.Path("easychat.txt")
val chatGpt = ChatGpt.defaultAuthLogToFile(logFile).allocated.map(_._1).Ø

val sysMessage = AiMessage.system("You are cow")
val userMessage = AiMessage.user("Make noise")

chatGpt.chat(List(sysMessage, userMessage)).Ø

```

# Streaming Chat

A simple chat application that uses the chat API to chat with an assistant.

```scala mdoc

import io.github.quafadas.dairect.*
import io.github.quafadas.dairect.ChatGpt.*
import cats.effect.IO
import ciris.*
import org.http4s.ember.client.EmberClientBuilder

val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource
val logger = fileLogger(Path("log.txt"))
val client = EmberClientBuilder.default[IO].build.map(authMiddleware(apikey))
val (chat, _) = ChatGpt.defaultAuthLogToFile(Path("log.txt")).allocated.Ø

val streamEasy = chat.stream(
    List(AiMessage.system("You are cow"), AiMessage.user("Make noise") ),
    authdClient = client
)

val streamed = streamEasy.debug().compile.toList

println(streamed.Ø.mkString(""))

```