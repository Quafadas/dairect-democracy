package io.github.quafadas.dairect

import munit.FunSuite
import smithy4s.json.Json
import io.github.quafadas.dairect.AssistantApi.Assistant
import smithy4s.Blob
import io.github.quafadas.dairect.MessagesApi.MessageDelta
import smithy4s.schema.Schema

abstract class ParseSuite extends FunSuite:
    def parseCheck[A: Schema](rawStr: String)(implicit loc: munit.Location) = 
        val parsedResult = Json.read[A](rawStr.blob)
        parsedResult.fold(
            error => fail(s"Parsing failed with error: $error"),
            identity
        )