package enums

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.boolean
import smithy4s.schema.Schema.struct

final case class IsFaceCardOutput(b: Option[Boolean] = None)
object IsFaceCardOutput extends ShapeTag.Companion[IsFaceCardOutput] {
  val id: ShapeId = ShapeId("enums", "IsFaceCardOutput")

  val hints: Hints = Hints(
    smithy.api.Output(),
  )

  implicit val schema: Schema[IsFaceCardOutput] = struct(
    boolean.optional[IsFaceCardOutput]("b", _.b),
  ){
    IsFaceCardOutput.apply
  }.withId(id).addHints(hints)
}
