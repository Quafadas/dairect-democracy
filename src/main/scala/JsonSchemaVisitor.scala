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


/*
  This visitor is supposed to extract everything needed to produce a JsonSchema from a smithy schema.

  Apparently Working
  - Primitive types
  - Document hint (descriptions)
  - Structs
  - packed inputs
  - bijection
  - Recursion
  - enumerations
  - List
  - Maps
  - Unions

  Currently not looked at

  - many shape restrictions / hints (e.g. string with a regex)

 */

trait JsonSchemaVisitor extends SchemaVisitor[JsonSchema]:

  override def nullable[A](schema: Schema[A]): JsonSchema[Option[A]] = ???

  override def map[K, V](shapeId1: ShapeId, hintsIn: Hints, key: Schema[K], value: Schema[V]): JsonSchema[Map[K, V]] =

    val valueSchema = this(value)
    new MapSchema[K, V](hintsIn):
      override val value: JsonSchema[V] = valueSchema
      override val shapeIdJ: Option[ShapeId] = shapeId1.some
    end new
  end map

  override def lazily[A](suspend: Lazy[Schema[A]]): JsonSchema[A] =
    val forShape = suspend.value.shapeId
    val rb = new RecursionBustingJsonSchemaVisitor(forShape) {}
    rb(suspend.value)
  end lazily

  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[smithy4s.schema.Schema, S, ?]],
      make: IndexedSeq[Any] => S
  ): JsonSchema[S] =
    val expandFields: Map[String, JsonSchema[?]] = fields.map { field =>
      field.label -> this(field.instance)
    }.toMap

    val requredIn: Set[String] = fields.filter(_.isRequired).map(_.label).toSet

    new StructSchemaIR[S](hints):
      override val required = requredIn
      override val fields: Map[String, JsonSchema[?]] = expandFields
      override val shapeIdJ: Option[ShapeId] = shapeId.some
    end new
  end struct

  override def enumeration[E](
      shapeIdIn: ShapeId,
      hintsIn: Hints,
      tagIn: EnumTag,
      valuesIn: List[EnumValue[E]],
      total: E => EnumValue[E]
  ): JsonSchema[E] =
    new EnumSchema[E](hintsIn):
      override val tag: EnumTag = tagIn
      override val values: List[EnumValue[E]] = valuesIn
      override val shapeIdJ: Option[ShapeId] = shapeIdIn.some
    end new
  end enumeration

  override def union[U](
      shapeId: ShapeId,
      hints: Hints,
      alternatives: Vector[Alt[smithy4s.schema.Schema, U, ?]],
      dispatch: Dispatcher[smithy4s.schema.Schema, U]
  ): JsonSchema[U] =
    hints match
      case Untagged.hint(_) =>
        val altSchemas = alternatives.map(alt => this(alt.instance))
        new UntaggedUnionSchema[U](hints):
          override val shapeIdJ: Option[ShapeId] = shapeId.some
          override val alts: Vector[JsonSchema[?]] = altSchemas
      case Discriminated.hint(d) =>
        ???
      case _                     =>
        val altSchemas = alternatives.map(alt => (alt.label, this(alt.instance)))
        new TaggedUnionSchema[U](hints):
          override val shapeIdJ: Option[ShapeId] = shapeId.some
          override val alts = altSchemas
    end match
  end union

  override def refine[A, B](schema: Schema[A], refinement: Refinement[A, B]): JsonSchema[B] =
    val target: JsonSchema[A] = this(schema)
    new JsonSchema[B]:
      override val hints: Hints = Hints.empty
      override val shapeIdJ: Option[ShapeId] = None
      val refineMe = target
      override val make: Map[String, Document] = refineMe.make
    end new
  end refine

  override def biject[A, B](schema: Schema[A], bijection: Bijection[A, B]): JsonSchema[B] =
    val target: JsonSchema[A] = this(schema)
    new BijectionJsonSchema[B](target, schema.hints):
      override val shapeIdJ: Option[ShapeId] = schema.shapeId.some
    end new
  end biject

  override def collection[C[_$2], A](
      shapeId: ShapeId,
      hints: Hints,
      tag: CollectionTag[C],
      member: Schema[A]
  ): JsonSchema[C[A]] =
    val childSchema = this(member)
    new ListJsonSchemaIR[C[A]](hints):
      override val shapeIdJ: Option[ShapeId] = shapeId.some
      override val child = childSchema
    end new
  end collection

  override def primitive[P](shapeId: ShapeId, hintsIn: Hints, tag: Primitive[P]): JsonSchema[P] =

    tag match

      case Primitive.PLong  => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hintsIn)
      case Primitive.PInt   => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hintsIn)
      case Primitive.PShort => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hintsIn)
      case Primitive.PByte  => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hintsIn)

      case Primitive.PFloat      => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hintsIn)
      case Primitive.PBigInt     => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hintsIn)
      case Primitive.PDouble     => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hintsIn)
      case Primitive.PBigDecimal => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hintsIn)

      case Primitive.PBoolean => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Boolean, shapeId.some, hintsIn)

      case Primitive.PUUID =>
        PrimitiveSchemaIR[P](JsonSchemaPrimitive.String, shapeId.some, hintsIn, formatIn = "uuid".some)
      case Primitive.PTimestamp => PrimitiveSchemaIR[P](JsonSchemaPrimitive.String, shapeId.some, hintsIn, formatIn = "date-time".some)
      case Primitive.PString    => PrimitiveSchemaIR[P](JsonSchemaPrimitive.String, shapeId.some, hintsIn)
      case PDocument => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Document, shapeId.some, hintsIn)
      case _                    => throw new Exception("This is not a primitive shape - we shouldn't get here")
    end match
  end primitive
end JsonSchemaVisitor
