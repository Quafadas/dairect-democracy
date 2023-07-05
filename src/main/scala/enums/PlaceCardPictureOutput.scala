package enums

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

final case class PlaceCardPictureOutput(s: Option[String] = None)
object PlaceCardPictureOutput extends ShapeTag.Companion[PlaceCardPictureOutput] {
  val id: ShapeId = ShapeId("enums", "PlaceCardPictureOutput")

  val hints: Hints = Hints(
    smithy.api.Output(),
  )

  implicit val schema: Schema[PlaceCardPictureOutput] = struct(
    string.optional[PlaceCardPictureOutput]("s", _.s),
  ){
    PlaceCardPictureOutput.apply
  }.withId(id).addHints(hints)
}
