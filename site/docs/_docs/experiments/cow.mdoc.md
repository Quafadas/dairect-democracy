---
title: Basic Chat
---

# Basic Chat

This is a simple chat application that uses ther chat API to chat with an assistant.

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