package weather

// Constant type
import smithy4s.*
import smithy4s.schema.*
import scala.*
import smithy4s.schema.Alt.Dispatcher
import smithy4s.schema.Schema.StructSchema
import cats.syntax.option.*
import smithy4s.Document.DNull

/*
  This class is supposed to extract everything needed to produce a JsonSchema from a smithy schema.

  Apparently Working
  - Primitive types
  - Document hint (descriptions)
  - Structs
  - packed inputs

  Currently not looked at
  - Nested structures (should work "free")
  - I'm not sure if the encdoing of all primitive types is correct. Needs testing
  - List
  - Recursion
  - Unions
  - Maps
  - Refinements (e.g. type myId = uuid)
  - shape restrictions / hints (e.g. string with a regex)


*/

trait JsonSchemaVisitor extends SchemaVisitor[JsonSchema]:

  private val alreadySeen: Set[ShapeId] = Set.empty

  def record(alreadySeen: Set[ShapeId]): JsonSchemaRecord = ???

  override def nullable[A](schema: Schema[A]): JsonSchema[Option[A]] = ???

  override def map[K, V](shapeId: ShapeId, hints: Hints, key: Schema[K], value: Schema[V]): JsonSchema[Map[K, V]] = ???

  override def lazily[A](suspend: Lazy[Schema[A]]): JsonSchema[A] = ???

  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[smithy4s.schema.Schema, S, ?]],
      make: IndexedSeq[Any] => S
  ): JsonSchema[S] =
    if(true)
      println("struct")
      println(shapeId)
      hints.all.foreach(println)
      println(fields.mkString(", "))
    end if


    val expandFields: Map[String, JsonSchema[?]] = fields.map { field =>
      field.label -> this(field.instance)
    }.toMap

    new StructSchemaIR[S]:
      override val fields: Map[String, JsonSchema[?]] = expandFields
      override val shapeIdJ: Option[ShapeId] = shapeId.some
      override val description: Option[String] = None
    end new
  end struct

  override def enumeration[E](
      shapeId: ShapeId,
      hints: Hints,
      tag: EnumTag,
      values: List[EnumValue[E]],
      total: E => EnumValue[E]
  ): JsonSchema[E] = ???

  override def union[U](
      shapeId: ShapeId,
      hints: Hints,
      alternatives: Vector[Alt[smithy4s.schema.Schema, U, ?]],
      dispatch: Dispatcher[smithy4s.schema.Schema, U]
  ): JsonSchema[U] = ???

  override def refine[A, B](schema: Schema[A], refinement: Refinement[A, B]): JsonSchema[B] = ???

  override def biject[A, B](schema: Schema[A], bijection: Bijection[A, B]): JsonSchema[B] = ???

  override def collection[C[_$2], A](
      shapeId: ShapeId,
      hints: Hints,
      tag: CollectionTag[C],
      member: Schema[A]
  ): JsonSchema[C[A]] = ???

  override def primitive[P](shapeId: ShapeId, hints: Hints, tag: Primitive[P]): JsonSchema[P] =
    hints.all.foreach(println)
    println(tag)
    println(shapeId)
    tag match

      case Primitive.PLong  => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Integer, shapeId.some)
      case Primitive.PInt   => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Integer, shapeId.some)
      case Primitive.PShort => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Integer, shapeId.some)
      case Primitive.PByte  => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Integer, shapeId.some)

      case Primitive.PFloat      => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some)
      case Primitive.PBigInt     => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some)
      case Primitive.PDouble     => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some)
      case Primitive.PBigDecimal => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some)

      case Primitive.PBoolean => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Boolean, shapeId.some)

      case Primitive.PUUID      => PrimitiveSchemaIR[P](JsonSchemaPrimitive.String, shapeId.some)
      case Primitive.PTimestamp => PrimitiveSchemaIR[P](JsonSchemaPrimitive.String, shapeId.some)
      case Primitive.PString    => PrimitiveSchemaIR[P](JsonSchemaPrimitive.String, shapeId.some)
      case _                    => throw new Exception("This is not a primitive shape - we shouldn't get here")
  end primitive
end JsonSchemaVisitor
