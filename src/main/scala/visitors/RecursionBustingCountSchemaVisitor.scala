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

trait RecursionBustingCountSchemaVisitor(val busted: scala.collection.mutable.Map[ShapeId, Double])
    extends ShapeCountSchemaVisitor:

  var bustCounter = 0

  override def lazily[A](suspend: Lazy[Schema[A]]): Noop[A] =
    val underlying = suspend.value.shapeId
    if !(busted.keySet.contains(suspend.value.shapeId)) then busted += (underlying -> Double.PositiveInfinity)
    this(suspend.value)
    Noop()
  end lazily

  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[S, ?]],
      make: IndexedSeq[Any] => S
  ): Noop[S] =
    if busted.get(shapeId).getOrElse(0.0).isFinite then super.struct(shapeId, hints, fields, make)
    Noop()
  end struct

end RecursionBustingCountSchemaVisitor

object RecursionBustingCountSchemaVisitor:

  def make(startShape: ShapeId) =
    val start = scala.collection.mutable.Map[ShapeId, Double]()
    new RecursionBustingCountSchemaVisitor(start) {}
  end make

end RecursionBustingCountSchemaVisitor
