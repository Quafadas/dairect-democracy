package smithyOpenAI

// import software.amazon.smithy.jsonschema.JsonSchemaConverter
// import software.amazon.smithy.model.Model
// import software.amazon.smithy.model.node.Node
import smithy4s.ShapeId

import cats.effect.IO
import cats.Id
import smithy4s.internals.DocumentEncoder
import smithy4s.Document
import smithy4s.http.json.JCodec
import smithy4s.schema.Schema

def makeDefs(forShapes: Set[ShapeId], schema: Schema[?]) =
  val jsonSchemas = for (shape <- forShapes) yield {

    val defsForThisShape = forShapes - shape
    val schemaforShapeVisitor = new JsonSchemaVisitorForShape(shape){}
    schemaforShapeVisitor(schema)
    val found = schemaforShapeVisitor.found

    val typCheck = found.map(subSchema =>
      println(subSchema.shapeId)
      val schemaVisitor = new JsonSchemaVisitor {}
      val internalRep = schemaVisitor(subSchema)
      Map( shape.name  -> Document.DObject(internalRep.makeWithDefs(defsForThisShape)))
    )
    typCheck
  }

  val hasSchema = jsonSchemas.flatten.fold(emptyMap)(_ ++ _)
  Map("definitions" -> hasSchema )

