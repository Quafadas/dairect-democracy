package people

import java.util.UUID
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct
import smithy4s.schema.Schema.uuid

final case class PetIdInput(id: UUID)
object PetIdInput extends ShapeTag.Companion[PetIdInput] {
  val id: ShapeId = ShapeId("people", "PetIdInput")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[PetIdInput] = struct(
    uuid.required[PetIdInput]("id", _.id).addHints(smithy.api.HttpLabel(), smithy.api.Required()),
  ){
    PetIdInput.apply
  }.withId(id).addHints(hints)
}
