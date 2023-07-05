package people

import smithy4s.Hints
import smithy4s.Newtype
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.schema.Schema.bijection
import smithy4s.schema.Schema.list

object People extends Newtype[List[Person]] {
  val id: ShapeId = ShapeId("people", "People")
  val hints: Hints = Hints.empty
  val underlyingSchema: Schema[List[Person]] = list(Person.schema).withId(id).addHints(hints)
  implicit val schema: Schema[People] = bijection(underlyingSchema, asBijection)
}
