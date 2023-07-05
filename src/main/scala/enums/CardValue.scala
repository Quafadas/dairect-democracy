package enums

import smithy4s.Hints
import smithy4s.Newtype
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.schema.Schema.bijection
import smithy4s.schema.Schema.int

object CardValue extends Newtype[Int] {
  val id: ShapeId = ShapeId("enums", "CardValue")
  val hints: Hints = Hints(
    smithy.api.Box(),
  )
  val underlyingSchema: Schema[Int] = int.withId(id).addHints(hints).validated(smithy.api.Range(min = Some(scala.math.BigDecimal(0.0)), max = Some(scala.math.BigDecimal(13.0))))
  implicit val schema: Schema[CardValue] = bijection(underlyingSchema, asBijection)
}
