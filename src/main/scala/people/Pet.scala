package people

import java.util.UUID
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct
import smithy4s.schema.Schema.uuid

final case class Pet(id: UUID, name: Option[String] = None, owner: Option[Person] = None)
object Pet extends ShapeTag.Companion[Pet] {
  val id: ShapeId = ShapeId("people", "Pet")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[Pet] = struct(
    uuid.required[Pet]("id", _.id).addHints(smithy.api.Required()),
    string.optional[Pet]("name", _.name),
    Person.schema.optional[Pet]("owner", _.owner),
  ){
    Pet.apply
  }.withId(id).addHints(hints)
}
