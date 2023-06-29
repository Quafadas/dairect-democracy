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

/* ++ emptyMap is the no-op for building our JSON schema */
def emptyMap = Map[String, Document]()

/*
  This is supposed to be our "basic" trait, which any and all schema-able entities _may_ implement.

  The idea, is that we'll end up with an object graph, looking exactly like the smithy Schema
  object graph, but with the addition of a "make" method, which will return a JSON schema

 */

trait JsonSchema[A]:
  val shapeIdJ: Option[ShapeId]
  val description: Option[String]

  def make: Map[String, Document] =
    //val addName = shapeIdJ.map(s => Map("name" -> Document.fromString(s.toString()))).getOrElse(emptyMap)
    val addDescription = description.map(s => Map("description" -> Document.fromString(s))).getOrElse(emptyMap)
    emptyMap ++ addDescription
  end make

end JsonSchema

/*

JSON schema has, apparently, these primitives:
  https://json-schema.org/understanding-json-schema/reference/type.html

Where I've made up my own definition of primitive as non-composite type

 */
enum JsonSchemaPrimitive:
  case String
  case Number
  case Integer
  case Boolean
  case Null // I don't think we need this?
end JsonSchemaPrimitive

trait PrimitiveSchemaIR[A] extends JsonSchema[A]:
  val typ: JsonSchemaPrimitive
  override def make: Map[String, Document] = super.make ++ Map("type" -> Document.fromString(typ.toString.toLowerCase()))

end PrimitiveSchemaIR

trait NonPrimitiveSchemaIR[A] extends JsonSchema[A]:
  val childeren: Map[ShapeId, JsonSchema[?]] = Map.empty
end NonPrimitiveSchemaIR

object PrimitiveSchemaIR:
  def apply[A](typIn: JsonSchemaPrimitive, shapeIdIn: Option[ShapeId]): PrimitiveSchemaIR[A] =
    new PrimitiveSchemaIR[A]:
      override val typ: JsonSchemaPrimitive = typIn
      override val shapeIdJ: Option[ShapeId] = shapeIdIn
      override val description: Option[String] = None
    end new
  end apply
end PrimitiveSchemaIR

trait StructSchemaIR[S] extends NonPrimitiveSchemaIR[S]:
  val fields: Map[String, JsonSchema[?]]
  override def make: Map[String, Document] =

    val fieldsJ = fields.map { case (k, v) => k -> Document.DObject(v.make) }
    val descriptionJ = description.map("description" -> Document.fromString(_))

    Map(
      // "name" -> Document.fromString(shapeIdJ.get.name),
      // "additionalProperties" -> Document.fromBoolean(false), // We should be as precise as possible?
      "parameters" -> Document.DObject(
        Map(
          "type" -> Document.fromString("object"),
          "properties" -> Document.DObject(fieldsJ)
        )
      )
    )

    // Document.DObject(tmp)
  end make

end StructSchemaIR

case class JsonSchemaRecord(nested: Map[ShapeId, Document], surface: Document.DObject)
