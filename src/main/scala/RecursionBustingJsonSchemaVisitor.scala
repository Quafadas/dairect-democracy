package weather

// Constant type
import smithy4s.*
import smithy4s.schema.*
import scala.*
import smithy4s.schema.Alt.Dispatcher
import smithy4s.schema.Schema.StructSchema
import cats.syntax.option.*
import smithy4s.Document.DNull
import cats.syntax.option.*
/*
  This class is supposed to extract everything needed to produce a JsonSchema from a smithy schema.

  Apparently Working
  - Primitive types
  - Document hint (descriptions)
  - Structs
  - packed inputs
  - bijection

  Currently not looked at
  - Nested structures (should work "free")
  - I'm not sure if the encdoing of all primitive types is correct. Needs testing
  - List
  - Recursion
  - Unions
  - Maps
  - shape restrictions / hints (e.g. string with a regex)
  - enumerations


 */

trait RecursionBustingJsonSchemaVisitor(val bustHere: ShapeId) extends JsonSchemaVisitor:

  var bustCounter = 0

  override def lazily[A](suspend: Lazy[Schema[A]]): JsonSchema[A] =
    this(suspend.value)

  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[smithy4s.schema.Schema, S, ?]],
      make: IndexedSeq[Any] => S
  ): JsonSchema[S] =

    if(shapeId == bustHere) {
      bustCounter = bustCounter + 1
    }
    if(bustCounter > 1) {
      new RecursiveSchema[S] {}
    } else {
      super.struct(shapeId, hints, fields, make)
    }

end RecursionBustingJsonSchemaVisitor
