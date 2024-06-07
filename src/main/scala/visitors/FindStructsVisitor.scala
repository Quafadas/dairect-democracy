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
import cats.syntax.all.*
import cats.kernel.Semigroup

class FindStructsVisitor extends SchemaVisitor[Noop]:

  private var structs: Set[ShapeId] = Set.empty

  def getStructs: Set[ShapeId] = structs

  override def enumeration[E](
      shapeId: ShapeId,
      hints: Hints,
      tag: EnumTag[E],
      values: List[EnumValue[E]],
      total: E => EnumValue[E]
  ): Noop[E] =
    Noop()
  end enumeration

  override def collection[C[_$2], A](
      shapeId: ShapeId,
      hints: Hints,
      tag: CollectionTag[C],
      member: Schema[A]
  ): Noop[C[A]] =
    this(member)
    Noop[C[A]]()
  end collection

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
    Noop()
  end primitive

  override def biject[A, B](schema: Schema[A], bijection: Bijection[A, B]): Noop[B] = Noop[B]()

  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[S, ?]],
      make: IndexedSeq[Any] => S
  ): Noop[S] =
    structs = structs ++ Set(shapeId)
    fields.foreach(field => this(field.instance))
    Noop()
  end struct

  override def refine[A, B](schema: Schema[A], refinement: Refinement[A, B]): Noop[B] = Noop[B]()

  override def lazily[A](suspend: Lazy[Schema[A]]): Noop[A] =
    val lzySchema = suspend.value
    if !structs.contains(lzySchema.shapeId) then
      structs = structs ++ Set(lzySchema.shapeId)
      super.apply(lzySchema)
    end if
    Noop()
  end lazily

  override def union[U](
      shapeId: ShapeId,
      hints: Hints,
      alternatives: Vector[Alt[U, ?]],
      dispatch: Dispatcher[U]
  ): Noop[U] =
    alternatives.foreach(alt => this(alt.instance))
    Noop[U]()
  end union

  override def option[A](schema: Schema[A]): Noop[Option[A]] = ???

end FindStructsVisitor
