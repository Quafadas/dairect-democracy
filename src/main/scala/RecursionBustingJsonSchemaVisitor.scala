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

  override def lazily[A](suspend: Lazy[Schema[A]]): JsonSchema[A] =
    if (!(busted.keySet.contains(suspend.value.shapeId))) {
      busted += (suspend.value.shapeId -> 0)
    }
    this(suspend.value)
  end lazily

  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[smithy4s.schema.Schema, S, ?]],
      make: IndexedSeq[Any] => S
  ): JsonSchema[S] =
    if busted.keySet.contains(shapeId) then
      busted.updateWith(shapeId)(currentCount =>
        currentCount match
          case None        => Some(1)
          case Some(value) => Some(value + 1)
      )
    end if
    if busted.get(shapeId).getOrElse(0) > 1 then new DefinitionSchema[S](shapeId.some) {}
    else super.struct(shapeId, hints, fields, make)

  end struct

end RecursionBustingJsonSchemaVisitor

trait RecursionBustingCountSchemaVisitor(val bustHere: ShapeId) extends ShapeCountSchemaVisitor:

  var bustCounter = 0

  override def lazily[A](suspend: Lazy[Schema[A]]): Noop[A] =
    Noop()

  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[smithy4s.schema.Schema, S, ?]],
      make: IndexedSeq[Any] => S
  ): Noop[S] =

    if shapeId == bustHere then bustCounter = bustCounter + 1
    if bustCounter < 1 then super.struct(shapeId, hints, fields, make)
    Noop()
  end struct

end RecursionBustingCountSchemaVisitor

object RecursionBustingJsonSchemaVisitor:

  def make(startShape: ShapeId) =
    val start = scala.collection.mutable.Map(startShape -> 0)
    new RecursionBustingJsonSchemaVisitor(start){}
  end make

end RecursionBustingJsonSchemaVisitor
