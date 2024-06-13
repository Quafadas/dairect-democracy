/*
 *  Copyright 2021-2022 Disney Streaming
 *
 *  Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     https://disneystreaming.github.io/TOST-1.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// Aggressively copy - pasted from
// https://github.com/disneystreaming/smithy4s/blob/5902d2fd81d1eee6703daaab6e2ad95eb69684ab/modules/test-utils/src/smithy4s/tests/DefaultSchemaVisitor.scala#L4

// I don't _think_ smithy exposes this functionality, because it's in the tests suite.
// Is there a world in which at least part of it might be in the public api?

package smithyOpenAI

import smithy4s.*
import smithy4s.Document.*

import cats.syntax.all.*
import cats.MonadThrow
import smithy4s.kinds.*
import cats.Applicative

import smithy4s.schema.*
import smithy4s.schema.Primitive.*
import cats.Id
import java.util.UUID
import smithy4s.json.Json

import software.amazon.smithy.model.node.Node
import com.github.plokhotnyuk.jsoniter_scala.core.JsonCodec
import openai.App.ChatCompletionResponseMessageFunctionCall
import scala.annotation.experimental
import openai.App.FunctionCall
import smithyOpenAI.schemaFromModel.InlineVisitor
import smithy4s.dynamic.DynamicSchemaIndex
import smithy4s.deriving.API

/** These are toy interpreters that turn services into json-in/json-out functions, and vice versa.
  *
  * Created for testing purposes.
  */
class SmithyOpenAIUtil[F[_]](implicit F: MonadThrow[F]):

  @experimental
  def toJsonSchema[Alg[_[_, _, _, _, _]]](
      alg: FunctorAlgebra[Alg, F]
  )(implicit S: Service[Alg]): Document =
    val unvalidatedModel = DynamicSchemaIndex.builder.addAll(S).build().toSmithyModel
    val docOpt = Json.readDocument(Node.prettyPrintJson(schemaFromModel(unvalidatedModel)))
    docOpt.toOption.get
  end toJsonSchema

  @experimental
  def openAiSmithyFunctionDispatch[Alg[_[_, _, _, _, _]]](
      alg: FunctorAlgebra[Alg, F]
  )(implicit S: Service[Alg]): FunctionCall => F[Document] =
    val transformation = S.toPolyFunction[Kind1[F]#toKind5](alg)
    val jsonEndpoints =
      S.endpoints.map(ep => ep.name -> toLowLevel(transformation, ep)).toMap

    (m: FunctionCall) =>
      val fctConfig: Document = smithy4s.json.Json.readDocument(m.arguments.get).getOrElse(???)
      println(fctConfig)
      val ep = jsonEndpoints.get(m.name)

      ep match
        case Some(jsonEndpoint) =>
          val fctResult = jsonEndpoint(fctConfig)
          fctResult
        case None => F.raiseError(NotFound)
      end match

  end openAiSmithyFunctionDispatch

  private def toLowLevel[Op[_, _, _, _, _], I, E, O, SI, SO](
      polyFunction: PolyFunction5[Op, Kind1[F]#toKind5],
      endpoint: Endpoint[Op, I, E, O, SI, SO]
  ): Document => F[Document] =
    implicit val decoderI = Document.Decoder.fromSchema(endpoint.input)
    implicit val encoderO = Document.Encoder.fromSchema(endpoint.output)
    implicit val encoderE: Document.Encoder[E] =
      endpoint.errorschema match
        case Some(errorableE) =>
          Document.Encoder.fromSchema(errorableE.schema)
        case None =>
          new Document.Encoder[E]:
            def encode(e: E): Document = Document.DNull
    (document: Document) =>
      for
        input <- document.decode[I].liftTo[F]
        op = endpoint.wrap(input)
        output <- (polyFunction(op): F[O]).map(encoderO.encode).recover { case endpoint.Error((_, e)) =>
          Document.obj("error" -> encoderE.encode(e))
        }
      yield output
  end toLowLevel

  case object NotFound extends Throwable
end SmithyOpenAIUtil
