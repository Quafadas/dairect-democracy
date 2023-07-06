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
import smithy4s.schema.Primitive.PBigInt
import smithy4s.schema.Primitive.PUUID
import smithy4s.schema.Primitive.PTimestamp
import smithy4s.schema.Schema.BijectionSchema
import smithy4s.http.matchPath
import smithy4s.schema.EnumTag.IntEnum
import smithy4s.schema.EnumTag.StringEnum

/*
  ++ emptyMap is the no-op for building our JSON schema - it has a fancy name in functional programming
  i.e. () in scala
  the 0 in integer addition, 1 in integer multiplication

  We'll need it a lot when we have optional fields.

 */
def emptyMap = Map[String, Document]()

/* Helper function to get document hints */
def extractDocHint(hints: Hints): Map[String, Document] =
  hints
    .get(smithy.api.Documentation)
    .map(desc => Map("description" -> Document.fromString(desc.toString())))
    .getOrElse(Map.empty[String, Document])
end extractDocHint

/* Helper function to get document hints */
def extractRefinementHint(hints: Hints): Map[String, Document] =
  hints
    .get(smithy.api.Range)
    .map(desc => Map("description" -> Document.fromString(desc.toString())))
    .getOrElse(Map.empty[String, Document])
end extractRefinementHint

/*
  This is supposed to be our "basic" trait, which any and all schema-able entities _may_ implement.

  The idea, is that we'll end up with an object graph, looking exactly like the smithy Schema
  object graph, but with the addition of a "make" method.

  The make method will lean into it's class heirachy, each class shoudl call it's superclass make.

 */
trait JsonSchema[A]:
  val hints: Hints
  val shapeIdJ: Option[ShapeId]
  lazy val description: Option[String] = hints.get(smithy.api.Documentation).map(_.toString())

  def make: Map[String, Document] =
    // val addName = shapeIdJ.map(s => Map("name" -> Document.fromString(s.toString()))).getOrElse(emptyMap)
    println(hints)
    val addDescription = description.map(s => Map("description" -> Document.fromString(s))).getOrElse(emptyMap)
    emptyMap ++ addDescription
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
  case Integer
  case Boolean
  case Null // I don't think we need this?
  case Document
end JsonSchemaPrimitive

/* */
trait PrimitiveSchemaIR[A] extends JsonSchema[A]:
  val typ: JsonSchemaPrimitive
  val format: Option[String]
  override def make: Map[String, Document] =
    val fmt = format.map(f => Map("format" -> Document.fromString(f))).getOrElse(emptyMap)
    super.make ++
      Map("type" -> Document.fromString(typ.toString.toLowerCase())) ++
      fmt
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
    new PrimitiveSchemaIR[A]:
      override val hints: Hints = hintsIn
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
  override def make: Map[String, Document] =
    val anyRequired = required.isEmpty match
      case false => Map("required" -> Document.DArray(required.map(Document.fromString).toIndexedSeq)),
      case true => emptyMap
    val fieldsJ = fields.map { case (k, v) => k -> Document.DObject(v.make) }
    super.make ++ Map(
      "type" -> Document.fromString("object"),
      "properties" -> Document.DObject(fieldsJ)
    ) ++ anyRequired
  end make

end StructSchemaIR

trait ListJsonSchemaIR[A](override val hints: Hints) extends NonPrimitiveSchemaIR[A]:
  val child: JsonSchema[?]
  override def make: Map[String, Document] =
    super.make ++ Map(
      "type" -> Document.fromString("array"),
      "items" -> Document.DObject(child.make)
    )
end ListJsonSchemaIR

trait BijectionJsonSchema[A](val bijectTarget: JsonSchema[?], override val hints: Hints)
    extends NonPrimitiveSchemaIR[A]:

  override def make: Map[String, Document] =
    super.make ++ bijectTarget.make

end BijectionJsonSchema

// case class JsonSchemaRecord(nested: Map[ShapeId, Document], surface: Document.DObject)

trait RecursiveSchema[A] extends JsonSchema[A]:
  override val hints: Hints = null // ooooof
  override val shapeIdJ: Option[ShapeId] = null // ooooof
  override def make: Map[String, Document] =
    Map(
      "$ref" -> Document.fromString("#")
    )

end RecursiveSchema

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

  override def make: Map[String, Document] = super.make ++ makeEnum

end EnumSchema


trait UntaggedUnionSchema[A](override val hints: Hints) extends JsonSchema[A]:
  val alts: Vector[JsonSchema[?]]

  override def make: Map[String, Document] =
    super.make ++ Map(
      "oneOf" -> Document.DArray(alts.map(a => Document.DObject(a.make)).toIndexedSeq)
    )

end UntaggedUnionSchema

trait TaggedUnionSchema[A](override val hints: Hints) extends JsonSchema[A]:
  val alts: Vector[(String, JsonSchema[?])]

  override def make: Map[String, Document] =
    super.make ++ Map(
      "oneOf" -> Document.DArray(alts.map(a =>
          Document.DObject(
            Map(a._1 -> Document.DObject( a._2.make))
          )
        ).toIndexedSeq
      )
    )

end TaggedUnionSchema