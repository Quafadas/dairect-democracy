package people

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

/** @param id
  *   The id of the person
  */
case class GetPersonInput(id: String)
object GetPersonInput extends ShapeTag.Companion[GetPersonInput] {
  val id: ShapeId = ShapeId("people", "GetPersonInput")

  val hints: Hints = Hints(
    smithy.api.Input(),
  )

  implicit val schema: Schema[GetPersonInput] = struct(
    string.required[GetPersonInput]("id", _.id).addHints(smithy.api.Documentation("The id of the person"), smithy.api.HttpLabel(), smithy.api.Required()),
  ){
    GetPersonInput.apply
  }.withId(id).addHints(hints)
}