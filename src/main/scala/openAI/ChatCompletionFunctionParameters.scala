package openAI

import smithy4s.Document
import smithy4s.Hints
import smithy4s.Newtype
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.schema.Schema.bijection
import smithy4s.schema.Schema.document

object ChatCompletionFunctionParameters extends Newtype[Document] {
  val id: ShapeId = ShapeId("openAI", "ChatCompletionFunctionParameters")
  val hints: Hints = Hints.empty
  val underlyingSchema: Schema[Document] = document.withId(id).addHints(hints)
  implicit val schema: Schema[ChatCompletionFunctionParameters] = bijection(underlyingSchema, asBijection)
}
