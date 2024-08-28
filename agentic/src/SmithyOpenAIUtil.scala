// Aggressively copy - pasted from
// https://github.com/disneystreaming/smithy4s/blob/5902d2fd81d1eee6703daaab6e2ad95eb69684ab/modules/test-utils/src/smithy4s/tests/DefaultSchemaVisitor.scala#L4

// I don't _think_ smithy exposes this functionality, because it's in the tests suite.
// Is there a world in which at least part of it might be in the public api?
package io.github.quafadas.dairect

import cats.MonadThrow
import cats.effect.IO
import cats.syntax.all.*
import smithy4s.*
import smithy4s.Document.*
import smithy4s.dynamic.DynamicSchemaIndex
import smithy4s.json.Json
import smithy4s.kinds.*
import smithy4s.schema.*
import software.amazon.smithy.model.node.Node

import scala.annotation.experimental

/** These are toy interpreters that turn services into json-in/json-out functions, and vice versa.
  *
  * Created for testing purposes.
  */
val ioToolGen = new SmithyOpenAIUtil[IO]

class SmithyOpenAIUtil[F[_]](implicit F: MonadThrow[F]):

  @experimental
  def toJsonSchema[Alg[_[_, _, _, _, _]]](
      alg: FunctorAlgebra[Alg, F]
  )(implicit S: Service[Alg]): Document =
    val unvalidatedModel = DynamicSchemaIndex.builder.addAll(S).build().toSmithyModel
    val docOpt = Json.readDocument(Node.prettyPrintJson(schemaFromModel(unvalidatedModel)))
    docOpt.toOption.get
  end toJsonSchema

  // format: off
  @experimental
  def openAiSmithyFunctionDispatch[Alg[_[_, _, _, _, _]]](
      alg: FunctorAlgebra[Alg, F]
  )(implicit S: Service[Alg]): FunctionCall => F[Document] =
    val transformation = S.toPolyFunction[Kind1[F]#toKind5](alg)
    val jsonEndpoints =
      S.endpoints.map(ep => ep.name -> toLowLevel(transformation, ep)).toMap

    (m: FunctionCall) =>
      smithy4s.json.Json.readDocument(m.arguments.get) match
        case Left(error) =>
          F.raiseError(new Throwable(s"Failed to parse arguments for $m .\n Error: $error"))
        case Right(fctConfig) =>
          jsonEndpoints.get(m.name) match
            case Some(jsonEndpoint) =>
              jsonEndpoint(fctConfig)
            case None => F.raiseError(new Throwable(s"Function $m not found"))
      end match  
  // format: on

  private def toLowLevel[Op[_, _, _, _, _], I, E, O, SI, SO](
      polyFunction: PolyFunction5[Op, Kind1[F]#toKind5],
      endpoint: Endpoint[Op, I, E, O, SI, SO]
  ): Document => F[Document] =
    given decoderI: Decoder[I] = Document.Decoder.fromSchema(endpoint.input)
    given encoderO: Encoder[O] = Document.Encoder.fromSchema(endpoint.output)
    given encoderE: Document.Encoder[E] =
      endpoint.errorschema match
        case Some(errorableE) =>
          Document.Encoder.fromSchema(errorableE.schema)
        case None =>
          new Document.Encoder[E]:
            def encode(e: E): Document = Document.DNull
    (document: Document) =>
      // println(Json.writeDocumentAsPrettyString(document))
      for
        input <- document.decode[I].liftTo[F]
        op = endpoint.wrap(input)
        output <- (polyFunction(op): F[O])
          .map { in =>
            encoderO.encode(in) match
              case Document.DObject(fields) => fields.get("value").getOrElse(???)
              case _                        => ???

          }
          .recover { case endpoint.Error((_, e)) =>
            Document.obj("error" -> encoderE.encode(e))
          }
      yield
      // println(Json.writeDocumentAsPrettyString(output))
      output
  end toLowLevel
end SmithyOpenAIUtil
