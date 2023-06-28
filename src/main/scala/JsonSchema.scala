package weather

// Constant type
import smithy4s.*
import smithy4s.schema.*
import scala.*
import smithy4s.schema.Alt.Dispatcher
import smithy4s.schema.Schema.StructSchema
import cats.syntax.option.*
import smithy4s.schema.Primitive.PBoolean
import smithy4s.Document.DNull

//type JsonSchemaIR[A] = ???

trait JsonSchemaVisitor extends SchemaVisitor[JsonSchema]:

  private val alreadySeen: Set[ShapeId] = Set.empty

  def record(alreadySeen: Set[ShapeId]): JsonSchemaRecord = ???

  override def nullable[A](schema: Schema[A]): JsonSchema[Option[A]] =
    ??? // Create a list. These fields are not mandatory.

  override def map[K, V](shapeId: ShapeId, hints: Hints, key: Schema[K], value: Schema[V]): JsonSchema[Map[K, V]] = ???

  override def lazily[A](suspend: Lazy[Schema[A]]): JsonSchema[A] = ???

  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[smithy4s.schema.Schema, S, ?]],
      make: IndexedSeq[Any] => S
  ): JsonSchema[S] =
    println("struct")
    println(shapeId)
    println(hints)
    println(fields.mkString(", "))

    val expandFields: Map[String, JsonSchema[?]] = fields.map { field =>
      field.label -> this(field.instance)
    }.toMap
    new StructSchemaIR[S]:
      override val fields: Map[String, JsonSchema[?]] = expandFields
      override val shapeIdJ: Option[ShapeId] = shapeId.some
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
    tag match
      case Primitive.PString =>
        new PrimitiveSchemaIR[P]:
          override val typ: JsonSchemaPrimitive = JsonSchemaPrimitive.String
          override val shapeIdJ: Option[ShapeId] = shapeId.some
      case PBoolean =>
        new PrimitiveSchemaIR[P]:
          override val typ: JsonSchemaPrimitive = JsonSchemaPrimitive.Boolean
          override val shapeIdJ: Option[ShapeId] = shapeId.some
end JsonSchemaVisitor

case class JsonSchemaRecord(nested: Map[ShapeId, Document], surface: Document.DObject)

trait JsonSchema[A]:
  val childeren: Map[ShapeId, JsonSchema[?]] = Map.empty
  val shapeIdJ: Option[ShapeId]
  val description: Option[String] = None

  private val emptyMap = Map[String, Document]()

  def make: Map[String, Document] =
    val baseMap = Map[String, Document]()
    val addName = shapeIdJ.map(s => Map("name" -> Document.fromString(s.toString()))).getOrElse(emptyMap)
    val addDescription = description.map(s => Map("description" -> Document.fromString(s))).getOrElse(emptyMap)
    baseMap ++ addName ++ addDescription
  end make

end JsonSchema

enum JsonSchemaPrimitive:
  case String
  case Number
  case Integer
  case Boolean
end JsonSchemaPrimitive

trait PrimitiveSchemaIR[A] extends JsonSchema[A]:
  val typ: JsonSchemaPrimitive
  override def make: Map[String, Document] = Map("type" -> Document.fromString(typ.toString.toLowerCase()))
end PrimitiveSchemaIR

trait StructSchemaIR[S] extends JsonSchema[S]:
  val fields: Map[String, JsonSchema[?]]
  override def make: Map[String, Document] =
    val startDoc = super.make
    val fieldsJ = fields.map { case (k, v) => k -> Document.DObject(v.make) }
    val descriptionJ = description.map("description" -> Document.fromString(_))


    Map(
      //"name" -> Document.fromString(shapeIdJ.get.name),
      //"additionalProperties" -> Document.fromBoolean(false), // We should be as precise as possible.
      "parameters" -> Document.DObject(
        Map(
          "type" -> Document.fromString("object"),
          "properties" -> Document.DObject(fieldsJ),
          )
      )
    )


    //Document.DObject(tmp)
  end make

end StructSchemaIR
