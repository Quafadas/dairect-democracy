package io.github.quafadas.dairect

import munit.FunSuite
import smithy4s.json.Json
import smithy4s.schema.Schema

abstract class ParseSuite extends FunSuite:
  def parseCheck[A: Schema](rawStr: String)(implicit loc: munit.Location) =
    val parsedResult = Json.read[A](rawStr.blob)
    parsedResult.fold(
      error => fail(s"Parsing failed with error: $error"),
      identity
    )
  end parseCheck
end ParseSuite
