package smithyOpenAI

// Constant type
import smithy4s.*
import smithy4s.schema.*
import scala.*
import smithy4s.schema.Alt.Dispatcher
import smithy4s.schema.Schema.StructSchema
import cats.syntax.option.*
import smithy4s.schema.Primitive.PBoolean
import smithy4s.Document.DNull
import smithy4s.schema.Primitive.PBigInt
import smithy4s.schema.Primitive.PUUID
import smithy4s.schema.Primitive.PTimestamp
import smithy4s.schema.Schema.BijectionSchema
import smithy4s.http.matchPath
import smithy4s.schema.EnumTag.IntEnum
import smithy4s.schema.EnumTag.StringEnum
import smithy4s.schema.Schema.MapSchema

/*
  ++ emptyMap is the no-op for building our JSON schema
  i.e. () in scala
  the 0 in integer addition, 1 in integer multiplication

  We'll need it a lot when we have optional fields.

 */
inline def emptyMap = Map[String, Document]()

/* Helper function to get document hints */
def extractDocHint(hints: Hints): Map[String, Document] =
  hints
    .get(smithy.api.Documentation)
    .map(desc => Map("description" -> Document.fromString(desc.toString())))
    .getOrElse(Map.empty[String, Document])
end extractDocHint

/*
  This is supposed to be our "basic" trait, which any and all schema-able entities _may_ implement.

  The idea, is that we'll end up with an object graph, looking exactly like the smithy Schema
  object graph, but with the addition of a "make" method.

  The make method will lean into it's class heirachy, each class shoudl call it's superclass make.

 */

val defRoot = "definitions"
trait JsonSchema[A]:
  val hints: Hints
  val shapeIdJ: Option[ShapeId]
  lazy val description: Option[String] = hints.get(smithy.api.Documentation).map(_.toString())

  def desc = description.map(d => Map("description" -> Document.fromString(d))).getOrElse(emptyMap)

  def makeWithDefs(defs: Set[ShapeId]): Map[String, Document] =
    shapeIdJ match
        case None => this.make(defs)
        case Some(shapeId) =>
          defs.contains(shapeId) match
            case true =>  new DefinitionSchema[A](shapeIdJ){}.make(Set())
            case false => this.make(defs)
  end makeWithDefs

  protected def make(defs: Set[ShapeId]): Map[String, Document] =
    emptyMap ++
      desc
  end make

end JsonSchema

/*

JSON schema has, apparently, these primitives:
  https://json-schema.org/understanding-json-schema/reference/type.html

Where I've made up my own definition of primitive as non-composite type and ditched array, object etc

 */
enum JsonSchemaPrimitive:
  case String
  case Number
  // case Integer
  case Boolean
  case Null // I don't think we need this?
  case Document
end JsonSchemaPrimitive

trait MayHaveDefault[A] extends JsonSchema[A]:
  lazy val defalt = hints.get(smithy.api.Default).map(d => d.value)

  override def make(defs: Set[ShapeId]): Map[String, Document] =
    val defal = defalt.map(d => Map("default" -> d)).getOrElse(emptyMap)
    super.make(defs) ++ defal
  end make
end MayHaveDefault

/* */
trait PrimitiveSchemaIR[A](override val hints: Hints) extends JsonSchema[A] with MayHaveDefault[A]:
  val typ: JsonSchemaPrimitive
  val format: Option[String]
  def mayHavePattern: Boolean = typ == JsonSchemaPrimitive.String
  def pattern: Map[String, Document] = mayHavePattern match
    case true =>
      hints.get(smithy.api.Pattern).map(p => Map("pattern" -> Document.fromString(p.toString()))).getOrElse(emptyMap)
    case false => emptyMap

  def mayHaveRange: Boolean = typ == JsonSchemaPrimitive.Number
  def min: Map[String, Document] = mayHaveRange match
    case true =>
      hints
        .get(smithy.api.Range)
        .flatMap(_.min)
        .map(p => Map("minimum" -> Document.fromBigDecimal(p)))
        .getOrElse(emptyMap)
    case false => emptyMap

  def max: Map[String, Document] = mayHaveRange match
    case true =>
      hints
        .get(smithy.api.Range)
        .flatMap(_.max)
        .map(p => Map("maximum" -> Document.fromBigDecimal(p)))
        .getOrElse(emptyMap)
    case false => emptyMap

  def fmt = format.map(f => Map("format" -> Document.fromString(f))).getOrElse(emptyMap)

  def typAdd = typ match
    case JsonSchemaPrimitive.Document => emptyMap
    case _                            => Map("type" -> Document.fromString(typ.toString.toLowerCase()))

  override def make(defs: Set[ShapeId]): Map[String, Document] =
    super.make(defs) ++
      typAdd ++
      fmt ++
      pattern ++
      min ++
      max

  end make

end PrimitiveSchemaIR

/*
  Easier constructor for PrimitiveSchemaIR
 */
object PrimitiveSchemaIR:
  def apply[A](
      typIn: JsonSchemaPrimitive,
      shapeIdIn: Option[ShapeId],
      hintsIn: Hints,
      formatIn: Option[String] = None
  ): PrimitiveSchemaIR[A] =
    new PrimitiveSchemaIR[A](hintsIn):
      override val typ: JsonSchemaPrimitive = typIn
      override val shapeIdJ: Option[ShapeId] = shapeIdIn
      override val format: Option[String] = formatIn
    end new
  end apply
end PrimitiveSchemaIR

trait NonPrimitiveSchemaIR[A] extends JsonSchema[A]:
//val childeren: Map[ShapeId, JsonSchema[?]] = Map.empty
end NonPrimitiveSchemaIR

trait StructSchemaIR[S](override val hints: Hints) extends NonPrimitiveSchemaIR[S]:
  val fields: Map[String, JsonSchema[?]]
  val required: Set[String]
  override def make(defs: Set[ShapeId]): Map[String, Document] =
    val anyRequired = required.isEmpty match
      case false => Map("required" -> Document.DArray(required.map(Document.fromString).toIndexedSeq))
      case true  => emptyMap
    val fieldsJ = fields.map { case (k, v) => k -> Document.DObject(v.makeWithDefs(defs)) }
    super.make(defs) ++ Map(
      "type" -> Document.fromString("object"),
      "properties" -> Document.DObject(fieldsJ)
    ) ++ anyRequired
  end make

end StructSchemaIR

trait ListJsonSchemaIR[A](override val hints: Hints) extends NonPrimitiveSchemaIR[A] with MayHaveDefault[A]:
  val child: JsonSchema[?]
  lazy val unique = hints.get(smithy.api.UniqueItems)
  override def make(defs: Set[ShapeId]): Map[String, Document] =
    super.make(defs) ++ Map(
      "type" -> Document.fromString("array"),
      "items" -> Document.DObject(child.makeWithDefs(defs))
    ) ++ unique.map(_ => Map("uniqueItems" -> Document.fromBoolean(true))).getOrElse(emptyMap)
end ListJsonSchemaIR

trait BijectionJsonSchema[A](val bijectTarget: JsonSchema[?], override val hints: Hints)
    extends NonPrimitiveSchemaIR[A]:

  override def make(defs: Set[ShapeId]): Map[String, Document] =
    super.make(defs) ++ bijectTarget.makeWithDefs(defs)

end BijectionJsonSchema

// case class JsonSchemaRecord(nested: Map[ShapeId, Document], surface: Document.DObject)

trait DefinitionSchema[A](override val shapeIdJ: Option[ShapeId]) extends JsonSchema[A]:
  override val hints: Hints = null // ooooof
  override def make(s: Set[ShapeId]): Map[String, Document] =
    val shapeId = shapeIdJ.get.name
    Map(
      "$ref" -> Document.fromString(s"#/$defRoot/$shapeId")
    )
  end make

end DefinitionSchema


trait EnumSchema[A](override val hints: Hints) extends JsonSchema[A]:
  val tag: EnumTag
  val values: List[EnumValue[A]]

  def makeEnum = tag match
    case IntEnum =>
      Map(
        "enum" -> Document.DArray(values.toIndexedSeq.map(v => Document.fromInt(v.intValue)))
      )
    case StringEnum =>
      Map(
        "enum" -> Document.DArray(values.toIndexedSeq.map(v => Document.fromString(v.stringValue)))
      )

  override def make(defs: Set[ShapeId]): Map[String, Document] = super.make(defs) ++ makeEnum

end EnumSchema

trait UntaggedUnionSchema[A](override val hints: Hints) extends JsonSchema[A]:
  val alts: Vector[JsonSchema[?]]

  override def make(defs: Set[ShapeId]): Map[String, Document] =
    super.make(defs) ++ Map(
      "oneOf" -> Document.DArray(alts.map(a => Document.DObject(a.makeWithDefs(defs))).toIndexedSeq)
    )

end UntaggedUnionSchema

trait TaggedUnionSchema[A](override val hints: Hints) extends JsonSchema[A]:
  val alts: Vector[(String, JsonSchema[?])]

  override def make(defs: Set[ShapeId]): Map[String, Document] =
    super.make(defs) ++ Map(
      "oneOf" -> Document.DArray(
        alts
          .map(a =>
            Document.DObject(
              Map(
                "type" -> Document.fromString("object"),
                "required" -> Document.DArray(IndexedSeq(Document.fromString(a._1))),
                "properties" -> Document.DObject(
                  Map(
                    a._1 -> Document.DObject(a._2.makeWithDefs(defs))
                  )
                ),
                "title" -> Document.fromString(a._1)
              )
            )
          )
          .toIndexedSeq
      )
    )

end TaggedUnionSchema

trait MapSchema[K, V](override val hints: Hints) extends JsonSchema[Map[K, V]]:
  val value: JsonSchema[V]

  override def make(defs: Set[ShapeId]): Map[String, Document] =
    super.make(defs) ++ Map(
      "type" -> Document.fromString("object"),
      "additionalProperties" -> Document.DObject(value.makeWithDefs(defs)),
      "propertyNames" -> Document.DObject(Map("type" -> Document.fromString("string")))
    )

end MapSchema
