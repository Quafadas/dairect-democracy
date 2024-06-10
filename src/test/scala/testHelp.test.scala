package smithyOpenAI

import software.amazon.smithy.jsonschema.JsonSchemaConverter
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.node.Node
import smithy4s.ShapeId

import cats.effect.IO
import cats.Id
import smithy4s.internals.DocumentEncoder
import smithy4s.Document
import smithy4s.schema.Schema

//import smithy4s.dynamic.DynamicSchemaIndex
import smithy4s.Schema
import smithy4s.dynamic.DynamicSchemaIndex
import java.net.URL
import smithy4s.json.Json

def smithy4sToSchema(ns: String, smithy: String, shape: String, defsOpt: Option[Set[ShapeId]] = None) =
  val myModel = toModel(ns, smithy)
  val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel)
  val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shape)).get

  val jsVisitor = new JsonSchemaVisitor {}
  val defs = defsOpt.getOrElse(Set[ShapeId]())
  val generatedSchema: Document = Document.DObject(jsVisitor(mySchema).makeWithDefs(defs))
  Json.writeDocumentAsPrettyString(generatedSchema)
end smithy4sToSchema

// def smithy4sToSchema2(ns: String, smithy: String, shape: String, defsOpt: Option[Set[ShapeId]] = None) =
//   val myModel = toModel(ns, smithy)
//   val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel)
//   val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shape)).get

// end smithy4sToSchema

def toModel(ns: String, smithy: String) = Model
  .assembler()
  .addUnparsedModel(
    s"$ns.smithy",
    smithy
  )
  .assemble()
  .unwrap()

def awsSmithyToSchema(ns: String, smithy: String, shape: String) =
  val r = toModel(ns, smithy)
  val amazonSchema = Node.printJson(
    JsonSchemaConverter
      .builder()
      .model(r)
      .build()
      .convertShape(
        r.expectShape(software.amazon.smithy.model.shapes.ShapeId.from(s"$ns#$shape"))
      )
      .toNode()
  )
  amazonSchema
end awsSmithyToSchema

def awsSmithyCompleteSchema(ns: String, smithy: String) =
  val r = toModel(ns, smithy)
  val amazonSchema = Node.printJson(
    JsonSchemaConverter
      .builder()
      .model(r)
      .build()
      .convert()
      .toNode()
  )
  amazonSchema
end awsSmithyCompleteSchema
