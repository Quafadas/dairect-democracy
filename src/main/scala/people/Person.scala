package people

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.recursive
import smithy4s.schema.Schema.struct

/** @param id
  *   The id of this person
  * @param childeren
  *   Childeren of this person
  */
case class Person(id: PersonId, childeren: Option[List[people.Person]] = None)
object Person extends ShapeTag.Companion[Person] {
  val id: ShapeId = ShapeId("people", "Person")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[Person] = recursive(struct(
    PersonId.schema.required[Person]("id", _.id).addHints(smithy.api.Documentation("The id of this person"), smithy.api.HttpLabel(), smithy.api.Required()),
    People.underlyingSchema.optional[Person]("childeren", _.childeren).addHints(smithy.api.Documentation("Childeren of this person")),
  ){
    Person.apply
  }.withId(id).addHints(hints))
}