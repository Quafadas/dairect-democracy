package openAI

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.bijection
import smithy4s.schema.Schema.union

/** Up to 4 sequences where the API will stop generating further tokens. */
sealed trait CreateChatCompletionRequestStop extends scala.Product with scala.Serializable {
  @inline final def widen: CreateChatCompletionRequestStop = this
}
object CreateChatCompletionRequestStop extends ShapeTag.Companion[CreateChatCompletionRequestStop] {
  val id: ShapeId = ShapeId("openAI", "CreateChatCompletionRequestStop")

  val hints: Hints = Hints(
    smithy.api.Documentation("Up to 4 sequences where the API will stop generating further tokens.\n"),
  )

  final case class Alt1Case(alt1: List[String]) extends CreateChatCompletionRequestStop

  object Alt1Case {
    val hints: Hints = Hints.empty
    val schema: Schema[Alt1Case] = bijection(CreateChatCompletionRequestStopOneOfAlt1.underlyingSchema.addHints(hints), Alt1Case(_), _.alt1)
    val alt = schema.oneOf[CreateChatCompletionRequestStop]("alt1")
  }

  implicit val schema: Schema[CreateChatCompletionRequestStop] = union(
    Alt1Case.alt,
  ){
    case c: Alt1Case => Alt1Case.alt(c)
  }.withId(id).addHints(hints)
}
