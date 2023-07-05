package enums

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

final case class DescribePlayingCardOutput(s: Option[String] = None)
object DescribePlayingCardOutput extends ShapeTag.Companion[DescribePlayingCardOutput] {
  val id: ShapeId = ShapeId("enums", "DescribePlayingCardOutput")

  val hints: Hints = Hints(
    smithy.api.Output(),
  )

  implicit val schema: Schema[DescribePlayingCardOutput] = struct(
    string.optional[DescribePlayingCardOutput]("s", _.s),
  ){
    DescribePlayingCardOutput.apply
  }.withId(id).addHints(hints)
}
