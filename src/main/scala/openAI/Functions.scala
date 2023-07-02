package openAI

import smithy4s.Hints
import smithy4s.Newtype
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.schema.Schema.bijection
import smithy4s.schema.Schema.list

/** A list of functions the model may generate JSON inputs for. */
object Functions extends Newtype[List[ChatCompletionFunctions]] {
  val id: ShapeId = ShapeId("openAI", "Functions")
  val hints: Hints = Hints(
    smithy.api.Documentation("A list of functions the model may generate JSON inputs for."),
  )
  val underlyingSchema: Schema[List[ChatCompletionFunctions]] = list(ChatCompletionFunctions.schema).withId(id).addHints(hints).validated(smithy.api.Length(min = Some(1L), max = None))
  implicit val schema: Schema[Functions] = bijection(underlyingSchema, asBijection)
}
