package smithyOpenAI

// Constant type
import smithy4s.*
import smithy4s.schema.*
import scala.*
import smithy4s.schema.Alt.Dispatcher
import smithy4s.schema.Schema.StructSchema
import cats.syntax.option.*
import smithy4s.Document.DNull
import cats.syntax.option.*
import smithy4s.schema.Schema.BijectionSchema
import alloy.Untagged
import alloy.Discriminated
import smithy4s.schema.Primitive.PDocument
import smithy4s.schema.Primitive.PBlob
import smithy4s.schema.Schema.PrimitiveSchema
import smithy4s.schema.Schema.MapSchema
import smithy4s.schema.Schema.EnumerationSchema
import smithy4s.schema.Schema.UnionSchema
import smithy4s.schema.Schema.RefinementSchema
import smithy4s.schema.Schema.LazySchema
import smithy4s.schema.Schema.CollectionSchema
import java.awt.Shape

class JsonSchemaFinderForShape(val forShape: ShapeId) extends ShapeCountSchemaVisitor:
  var seen = Set[ShapeId]()
  var found: Option[Schema[?]] = None
  override def apply[A](schema: Schema[A]) =
    if schema.shapeId == forShape then found = Some(schema)
    if found.isDefined then Noop[A]()
    super.apply(schema)
  end apply

  override def lazily[A](suspend: Lazy[Schema[A]]): Noop[A] =
    val schema = suspend.value
    if schema.shapeId == forShape then
      found = Some(schema)
      Noop[A]()
    else if seen.contains(schema.shapeId) then Noop[A]()
    else
      seen = seen ++ Set(schema.shapeId)
      this(schema)
    end if
  end lazily

end JsonSchemaFinderForShape
