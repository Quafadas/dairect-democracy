package people

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

/** @param person
  *   A description of the weather in the city
  */
final case class GetPersonOutput(person: Person)
object GetPersonOutput extends ShapeTag.Companion[GetPersonOutput] {
  val id: ShapeId = ShapeId("people", "GetPersonOutput")

  val hints: Hints = Hints(
    smithy.api.Output(),
  )

  implicit val schema: Schema[GetPersonOutput] = struct(
    Person.schema.required[GetPersonOutput]("person", _.person).addHints(smithy.api.Documentation("A description of the weather in the city"), smithy.api.Required()),
  ){
    GetPersonOutput.apply
  }.withId(id).addHints(hints)
}
