package people

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

final case class GetChilderenOutput(childeren: Option[List[Person]] = None)
object GetChilderenOutput extends ShapeTag.Companion[GetChilderenOutput] {
  val id: ShapeId = ShapeId("people", "GetChilderenOutput")

  val hints: Hints = Hints(
    smithy.api.Output(),
  )

  implicit val schema: Schema[GetChilderenOutput] = struct(
    People.underlyingSchema.optional[GetChilderenOutput]("childeren", _.childeren),
  ){
    GetChilderenOutput.apply
  }.withId(id).addHints(hints)
}
