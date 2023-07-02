package openAI

import smithy4s.Endpoint
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.Service
import smithy4s.ShapeId
import smithy4s.StreamingSchema
import smithy4s.Transformation
import smithy4s.kinds.PolyFunction5
import smithy4s.kinds.toPolyFunction5.const5

trait OpenAIServiceGen[F[_, _, _, _, _]] {
  self =>

  def createChatCompletion(body: CreateChatCompletionRequest): F[CreateChatCompletionInput, Nothing, CreateChatCompletion200, Nothing, Nothing]

  def transform: Transformation.PartiallyApplied[OpenAIServiceGen[F]] = Transformation.of[OpenAIServiceGen[F]](this)
}

object OpenAIServiceGen extends Service.Mixin[OpenAIServiceGen, OpenAIServiceOperation] {

  val id: ShapeId = ShapeId("openAI", "OpenAIService")
  val version: String = ""

  val hints: Hints = Hints(
    alloy.SimpleRestJson(),
  )

  def apply[F[_]](implicit F: Impl[F]): F.type = F

  object ErrorAware {
    def apply[F[_, _]](implicit F: ErrorAware[F]): F.type = F
    type Default[F[+_, +_]] = Constant[smithy4s.kinds.stubs.Kind2[F]#toKind5]
  }

  val endpoints: List[smithy4s.Endpoint[OpenAIServiceOperation, _, _, _, _, _]] = List(
    OpenAIServiceOperation.CreateChatCompletion,
  )

  def endpoint[I, E, O, SI, SO](op: OpenAIServiceOperation[I, E, O, SI, SO]) = op.endpoint
  class Constant[P[-_, +_, +_, +_, +_]](value: P[Any, Nothing, Nothing, Nothing, Nothing]) extends OpenAIServiceOperation.Transformed[OpenAIServiceOperation, P](reified, const5(value))
  type Default[F[+_]] = Constant[smithy4s.kinds.stubs.Kind1[F]#toKind5]
  def reified: OpenAIServiceGen[OpenAIServiceOperation] = OpenAIServiceOperation.reified
  def mapK5[P[_, _, _, _, _], P1[_, _, _, _, _]](alg: OpenAIServiceGen[P], f: PolyFunction5[P, P1]): OpenAIServiceGen[P1] = new OpenAIServiceOperation.Transformed(alg, f)
  def fromPolyFunction[P[_, _, _, _, _]](f: PolyFunction5[OpenAIServiceOperation, P]): OpenAIServiceGen[P] = new OpenAIServiceOperation.Transformed(reified, f)
  def toPolyFunction[P[_, _, _, _, _]](impl: OpenAIServiceGen[P]): PolyFunction5[OpenAIServiceOperation, P] = OpenAIServiceOperation.toPolyFunction(impl)

}

sealed trait OpenAIServiceOperation[Input, Err, Output, StreamedInput, StreamedOutput] {
  def run[F[_, _, _, _, _]](impl: OpenAIServiceGen[F]): F[Input, Err, Output, StreamedInput, StreamedOutput]
  def endpoint: (Input, Endpoint[OpenAIServiceOperation, Input, Err, Output, StreamedInput, StreamedOutput])
}

object OpenAIServiceOperation {

  object reified extends OpenAIServiceGen[OpenAIServiceOperation] {
    def createChatCompletion(body: CreateChatCompletionRequest) = CreateChatCompletion(CreateChatCompletionInput(body))
  }
  class Transformed[P[_, _, _, _, _], P1[_ ,_ ,_ ,_ ,_]](alg: OpenAIServiceGen[P], f: PolyFunction5[P, P1]) extends OpenAIServiceGen[P1] {
    def createChatCompletion(body: CreateChatCompletionRequest) = f[CreateChatCompletionInput, Nothing, CreateChatCompletion200, Nothing, Nothing](alg.createChatCompletion(body))
  }

  def toPolyFunction[P[_, _, _, _, _]](impl: OpenAIServiceGen[P]): PolyFunction5[OpenAIServiceOperation, P] = new PolyFunction5[OpenAIServiceOperation, P] {
    def apply[I, E, O, SI, SO](op: OpenAIServiceOperation[I, E, O, SI, SO]): P[I, E, O, SI, SO] = op.run(impl) 
  }
  final case class CreateChatCompletion(input: CreateChatCompletionInput) extends OpenAIServiceOperation[CreateChatCompletionInput, Nothing, CreateChatCompletion200, Nothing, Nothing] {
    def run[F[_, _, _, _, _]](impl: OpenAIServiceGen[F]): F[CreateChatCompletionInput, Nothing, CreateChatCompletion200, Nothing, Nothing] = impl.createChatCompletion(input.body)
    def endpoint: (CreateChatCompletionInput, smithy4s.Endpoint[OpenAIServiceOperation,CreateChatCompletionInput, Nothing, CreateChatCompletion200, Nothing, Nothing]) = (input, CreateChatCompletion)
  }
  object CreateChatCompletion extends smithy4s.Endpoint[OpenAIServiceOperation,CreateChatCompletionInput, Nothing, CreateChatCompletion200, Nothing, Nothing] {
    val id: ShapeId = ShapeId("openAI", "CreateChatCompletion")
    val input: Schema[CreateChatCompletionInput] = CreateChatCompletionInput.schema.addHints(smithy4s.internals.InputOutput.Input.widen)
    val output: Schema[CreateChatCompletion200] = CreateChatCompletion200.schema.addHints(smithy4s.internals.InputOutput.Output.widen)
    val streamedInput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val streamedOutput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val hints: Hints = Hints(
      smithy.api.Http(method = smithy.api.NonEmptyString("POST"), uri = smithy.api.NonEmptyString("/chat/completions"), code = 200),
    )
    def wrap(input: CreateChatCompletionInput) = CreateChatCompletion(input)
    override val errorable: Option[Nothing] = None
  }
}

