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
import smithy4s.schema.Schema.BijectionSchema
import alloy.Untagged
import alloy.Discriminated


/*
  This class is supposed to extract everything needed to produce a JsonSchema from a smithy schema.

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

  override def map[K, V](shapeId: ShapeId, hints: Hints, key: Schema[K], value: Schema[V]): JsonSchema[Map[K, V]] =
    val keySchema = this(key)
    val valueSchema = this(value)
    new JsonSchema[Map[K, V]]:
      override val hints: Hints = hints
      override val shapeIdJ: Option[ShapeId] = shapeId.some
      override val make: Map[String, Document] = super.make ++ Map[String, Document](
        "type" -> Document.fromString("object"),
        "additionalProperties" -> Document.DObject(valueSchema.make)
      )
    end new
  end map

  override def lazily[A](suspend: Lazy[Schema[A]]): JsonSchema[A] =
    println("lazily")
    println(suspend.value)

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
    if true then
      println("struct")
      println(shapeId)
      hints.all.foreach(println)
      println(fields.mkString(", "))
    end if

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
    println("enumeration")
    println(shapeIdIn)
    println(hintsIn)
    println(tagIn)
    println(valuesIn)
    println(total)
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
    println("union")
    println(shapeId)
    println(hints)
    println(alternatives)


    alternatives.foreach { alt =>
      println(alt.label)
      println(alt.instance)
    }


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
    println("refine")
    println(schema)
    println(refinement)
    val target: JsonSchema[A] = this(schema)
    new JsonSchema[B]:
      override val hints: Hints = Hints.empty
      override val shapeIdJ: Option[ShapeId] = None
      val refineMe = target
      override val make: Map[String, Document] = refineMe.make
    end new
  end refine

  override def biject[A, B](schema: Schema[A], bijection: Bijection[A, B]): JsonSchema[B] =
    println("biject")
    println(schema)
    // println(A.getClass)
    // println(B.getClass)

    println(bijection.toString())
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

  override def primitive[P](shapeId: ShapeId, hints: Hints, tag: Primitive[P]): JsonSchema[P] =
    hints.all.foreach(println)
    println(tag)
    println(shapeId)
    tag match

      case Primitive.PLong  => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Integer, shapeId.some, hints)
      case Primitive.PInt   => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Integer, shapeId.some, hints)
      case Primitive.PShort => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Integer, shapeId.some, hints)
      case Primitive.PByte  => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Integer, shapeId.some, hints)

      case Primitive.PFloat      => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hints)
      case Primitive.PBigInt     => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hints)
      case Primitive.PDouble     => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hints)
      case Primitive.PBigDecimal => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Number, shapeId.some, hints)

      case Primitive.PBoolean => PrimitiveSchemaIR[P](JsonSchemaPrimitive.Boolean, shapeId.some, hints)

      case Primitive.PUUID =>
        PrimitiveSchemaIR[P](JsonSchemaPrimitive.String, shapeId.some, hints, formatIn = "uuid".some)
      case Primitive.PTimestamp => PrimitiveSchemaIR[P](JsonSchemaPrimitive.String, shapeId.some, hints)
      case Primitive.PString    => PrimitiveSchemaIR[P](JsonSchemaPrimitive.String, shapeId.some, hints)
      case _                    => throw new Exception("This is not a primitive shape - we shouldn't get here")
    end match
  end primitive
end JsonSchemaVisitor
