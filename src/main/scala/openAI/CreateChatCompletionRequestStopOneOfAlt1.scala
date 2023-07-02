package openAI

import smithy4s.Hints
import smithy4s.Newtype
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.schema.Schema.bijection
import smithy4s.schema.Schema.list
import smithy4s.schema.Schema.string

object CreateChatCompletionRequestStopOneOfAlt1 extends Newtype[List[String]] {
  val id: ShapeId = ShapeId("openAI", "CreateChatCompletionRequestStopOneOfAlt1")
  val hints: Hints = Hints.empty
  val underlyingSchema: Schema[List[String]] = list(string).withId(id).addHints(hints).validated(smithy.api.Length(min = Some(1L), max = Some(4L)))
  implicit val schema: Schema[CreateChatCompletionRequestStopOneOfAlt1] = bijection(underlyingSchema, asBijection)
}
