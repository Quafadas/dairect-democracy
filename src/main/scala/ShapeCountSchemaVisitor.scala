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
import java.awt.Shape
import scala.collection.mutable.Map
import scala.collection.immutable

val countMe = new ShapeCountSchemaVisitor {}

trait ShapeCountSchemaVisitor extends SchemaVisitor[Noop]:

  private def incrementObserved(shapeId: ShapeId) =
    observed.updateWith(shapeId)(currentCount =>
      currentCount match
        case None => Some(1)
        case Some(value) => Some(value + 1)
    )

  // Should be Map[ShapeId, Double]
  private val observed: Map[ShapeId, Double] = Map.empty

  def getCounts : scala.collection.immutable.Map[ShapeId, Double] = observed.toMap

  override def enumeration[E](
      shapeId: ShapeId,
      hints: Hints,
      tag: EnumTag,
      values: List[EnumValue[E]],
      total: E => EnumValue[E]
  ): Noop[E] =
    incrementObserved(shapeId)
    Noop()

  override def collection[C[_$2], A](
      shapeId: ShapeId,
      hints: Hints,
      tag: CollectionTag[C],
      member: Schema[A]
  ): Noop[C[A]] = ???

  override def map[K, V](
      shapeId: ShapeId,
      hints: Hints,
      key: Schema[K],
      value: Schema[V]
  ): Noop[scala.collection.immutable.Map[K, V]] =
    this(value)

    Noop()
  end map

  override def primitive[P](shapeId: ShapeId, hints: Hints, tag: Primitive[P]): Noop[P] =
    incrementObserved(shapeId)
    Noop()

  override def biject[A, B](schema: Schema[A], bijection: Bijection[A, B]): Noop[B] = ???

  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[smithy4s.schema.Schema, S, ?]],
      make: IndexedSeq[Any] => S
  ): Noop[S] =
    fields.foreach(field => this(field.instance))
    incrementObserved(shapeId)
    Noop()
  end struct

  override def refine[A, B](schema: Schema[A], refinement: Refinement[A, B]): Noop[B] = ???

  override def lazily[A](suspend: Lazy[Schema[A]]): Noop[A] =
    val otherFields = new RecursionBustingCountSchemaVisitor(suspend.value.shapeId) {}
    otherFields(suspend.value)
    observed += (suspend.value.shapeId -> Double.PositiveInfinity)
    Noop()
  end lazily

  override def union[U](
      shapeId: ShapeId,
      hints: Hints,
      alternatives: Vector[Alt[smithy4s.schema.Schema, U, ?]],
      dispatch: Dispatcher[smithy4s.schema.Schema, U]
  ): Noop[U] = ???

  override def nullable[A](schema: Schema[A]): Noop[Option[A]] = ???

end ShapeCountSchemaVisitor

trait Noop[A] {}

object Noop:
  def apply[A]() = new Noop[A] {}
end Noop
