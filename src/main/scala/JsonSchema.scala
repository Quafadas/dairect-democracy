package weather

// Constant type
import smithy4s._
import smithy4s.schema._
import scala.*
import smithy4s.schema.Alt.Dispatcher

//type JsonSchemaIR[A] = ???

trait JsonSchemaVisitor extends SchemaVisitor[JsonSchema] {

  private val alreadySeen: Set[ShapeId] = Set.empty

  def record(alreadySeen: Set[ShapeId]) : JsonSchemaRecord = ???

  override def nullable[A](schema: Schema[A]): JsonSchema[Option[A]] = ??? // Create a list. These fields are not mandatory.

  override def map[K, V](shapeId: ShapeId, hints: Hints, key: Schema[K], value: Schema[V]): JsonSchema[Map[K, V]] = ???

  override def lazily[A](suspend: Lazy[Schema[A]]): JsonSchema[A] = ???

  override def struct[S](shapeId: ShapeId, hints: Hints, fields: Vector[Field[smithy4s.schema.Schema, S, ?]], make: IndexedSeq[Any] => S): JsonSchema[S] =
    println("struct")
    println(shapeId)
    println(hints)
    println(fields.mkString(", "))

    val expandFields: Map[String, JsonSchema[?]] = fields.map { field =>
      field.label -> this(field.instance)
    }.toMap


    new JsonSchema[S] {
      override val childeren: Map[ShapeId, JsonSchema[?]] = expandFields
      // fields.map { field =>
      //   field.label -> ???
      // }.toMap
    }

  override def enumeration[E](shapeId: ShapeId, hints: Hints, tag: EnumTag, values: List[EnumValue[E]], total: E => EnumValue[E]): JsonSchema[E] = ???

  override def union[U](shapeId: ShapeId, hints: Hints, alternatives: Vector[Alt[smithy4s.schema.Schema, U, ?]], dispatch: Dispatcher[smithy4s.schema.Schema, U]): JsonSchema[U] = ???

  override def refine[A, B](schema: Schema[A], refinement: Refinement[A, B]): JsonSchema[B] = ???

  override def biject[A, B](schema: Schema[A], bijection: Bijection[A, B]): JsonSchema[B] = ???

  override def collection[C[_$2], A](shapeId: ShapeId, hints: Hints, tag: CollectionTag[C], member: Schema[A]): JsonSchema[C[A]] = ???

  override def primitive[P](shapeId: ShapeId, hints: Hints, tag: Primitive[P]): JsonSchema[P] = ???



}

case class JsonSchemaRecord(nested: Map[ShapeId, Document], surface: Document.DObject)

trait JsonSchema[A] {
    // `alreadySeen` should track the shapes you've already visited, to avoid looping.
    //def record(alreadySeen: Set[ShapeId]) : JsonSchemaRecord
    val childeren : Map[ShapeId, JsonSchema[_]] = Map.empty

   final def make: Document = {
     //val jsonSchemaRecord = record(Set.empty)
     // take a `JsonSchemaRecord` and create a Document.DObject with the `$defs`
     // see https://json-schema.org/understanding-json-schema/structuring.html#defs
     ???
   }
}