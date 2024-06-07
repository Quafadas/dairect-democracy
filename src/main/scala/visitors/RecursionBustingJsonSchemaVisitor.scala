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

/*

This sets out to prevent looping in a visitor.

 */
trait RecursionBustingJsonSchemaVisitor(val busted: scala.collection.mutable.Map[ShapeId, Int])
    extends JsonSchemaVisitor:

  /*
    When a shape passes through here we note that it would enter a loop.
   */
  override def lazily[A](suspend: Lazy[Schema[A]]): JsonSchema[A] =
    val lzySchema = suspend.value
    if !busted.keySet.contains(lzySchema.shapeId) then
      busted.updateWith(lzySchema.shapeId)(currentCount =>
        currentCount match
          case None        => Some(0)
          case Some(value) => Some(value)
      )
    end if
    this(lzySchema)
  end lazily

  /*
    Count the number of times we've seen a potentially looping shape.
    We want to visit it once.
   */
  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[smithy4s.schema.Field[S, ?]],
      make: IndexedSeq[Any] => S
  ): JsonSchema[S] =
    if busted.keySet.contains(shapeId) then
      busted.updateWith(shapeId)(currentCount =>
        currentCount match
          case None        => None
          case Some(value) => Some(value + 1)
      )
    end if
    if busted.get(shapeId).getOrElse(0) > 1 then new DefinitionSchema[S](shapeId.some) {}
    else super.struct(shapeId, hints, fields, make)

  end struct

end RecursionBustingJsonSchemaVisitor

object RecursionBustingJsonSchemaVisitor:

  def make(startShape: ShapeId) =
    val start = scala.collection.mutable.Map(startShape -> 0)
    new RecursionBustingJsonSchemaVisitor(start) {}
  end make

end RecursionBustingJsonSchemaVisitor
