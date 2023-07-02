package openAI

import smithy4s.Hints
import smithy4s.Newtype
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.schema.Schema.bijection
import smithy4s.schema.Schema.list

object CreateChatCompletionResponseChoices extends Newtype[List[CreateChatCompletionResponseChoicesItem]] {
  val id: ShapeId = ShapeId("openAI", "CreateChatCompletionResponseChoices")
  val hints: Hints = Hints.empty
  val underlyingSchema: Schema[List[CreateChatCompletionResponseChoicesItem]] = list(CreateChatCompletionResponseChoicesItem.schema).withId(id).addHints(hints)
  implicit val schema: Schema[CreateChatCompletionResponseChoices] = bijection(underlyingSchema, asBijection)
}
