package people

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.int
import smithy4s.schema.Schema.struct

final case class FamilyTreeDepthOutput(depth: Option[Int] = None)
object FamilyTreeDepthOutput extends ShapeTag.Companion[FamilyTreeDepthOutput] {
  val id: ShapeId = ShapeId("people", "FamilyTreeDepthOutput")

  val hints: Hints = Hints(
    smithy.api.Output(),
  )

  implicit val schema: Schema[FamilyTreeDepthOutput] = struct(
    int.optional[FamilyTreeDepthOutput]("depth", _.depth),
  ){
    FamilyTreeDepthOutput.apply
  }.withId(id).addHints(hints)
}
