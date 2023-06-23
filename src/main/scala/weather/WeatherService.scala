package weather

import smithy4s.Endpoint
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.Service
import smithy4s.ShapeId
import smithy4s.StreamingSchema
import smithy4s.Transformation
import smithy4s.kinds.PolyFunction5
import smithy4s.kinds.toPolyFunction5.const5
import smithy4s.Errorable

trait WeatherServiceGen[F[_, _, _, _, _]] {
  self =>

  /** Get the weather for a city
    * @param location
    *   The name of the city
    */
  def getWeather(location: String): F[GetWeatherInput, Nothing, GetWeatherOutput, Nothing, Nothing]

  def transform: Transformation.PartiallyApplied[WeatherServiceGen[F]] = Transformation.of[WeatherServiceGen[F]](this)
}

object WeatherServiceGen extends Service.Mixin[WeatherServiceGen, WeatherServiceOperation] {

  val id: ShapeId = ShapeId("weather", "WeatherService")
  val version: String = ""

  val hints: Hints = Hints(
    alloy.SimpleRestJson(),
  )

  def apply[F[_]](implicit F: Impl[F]): F.type = F

  object ErrorAware {
    def apply[F[_, _]](implicit F: ErrorAware[F]): F.type = F
    type Default[F[+_, +_]] = Constant[smithy4s.kinds.stubs.Kind2[F]#toKind5]
  }

  val endpoints: List[smithy4s.Endpoint[WeatherServiceOperation, _, _, _, _, _]] = List(
    WeatherServiceOperation.GetWeather,
  )

  def endpoint[I, E, O, SI, SO](op: WeatherServiceOperation[I, E, O, SI, SO]) = op.endpoint
  class Constant[P[-_, +_, +_, +_, +_]](value: P[Any, Nothing, Nothing, Nothing, Nothing]) extends WeatherServiceOperation.Transformed[WeatherServiceOperation, P](reified, const5(value))
  type Default[F[+_]] = Constant[smithy4s.kinds.stubs.Kind1[F]#toKind5]
  def reified: WeatherServiceGen[WeatherServiceOperation] = WeatherServiceOperation.reified
  def mapK5[P[_, _, _, _, _], P1[_, _, _, _, _]](alg: WeatherServiceGen[P], f: PolyFunction5[P, P1]): WeatherServiceGen[P1] = new WeatherServiceOperation.Transformed(alg, f)
  def fromPolyFunction[P[_, _, _, _, _]](f: PolyFunction5[WeatherServiceOperation, P]): WeatherServiceGen[P] = new WeatherServiceOperation.Transformed(reified, f)
  def toPolyFunction[P[_, _, _, _, _]](impl: WeatherServiceGen[P]): PolyFunction5[WeatherServiceOperation, P] = WeatherServiceOperation.toPolyFunction(impl)

}

sealed trait WeatherServiceOperation[Input, Err, Output, StreamedInput, StreamedOutput] {
  def run[F[_, _, _, _, _]](impl: WeatherServiceGen[F]): F[Input, Err, Output, StreamedInput, StreamedOutput]
  def endpoint: (Input, Endpoint[WeatherServiceOperation, Input, Err, Output, StreamedInput, StreamedOutput])
}

object WeatherServiceOperation {

  object reified extends WeatherServiceGen[WeatherServiceOperation] {
    def getWeather(location: String) = GetWeather(GetWeatherInput(location))
  }
  class Transformed[P[_, _, _, _, _], P1[_ ,_ ,_ ,_ ,_]](alg: WeatherServiceGen[P], f: PolyFunction5[P, P1]) extends WeatherServiceGen[P1] {
    def getWeather(location: String) = f[GetWeatherInput, Nothing, GetWeatherOutput, Nothing, Nothing](alg.getWeather(location))
  }

  def toPolyFunction[P[_, _, _, _, _]](impl: WeatherServiceGen[P]): PolyFunction5[WeatherServiceOperation, P] = new PolyFunction5[WeatherServiceOperation, P] {
    def apply[I, E, O, SI, SO](op: WeatherServiceOperation[I, E, O, SI, SO]): P[I, E, O, SI, SO] = op.run(impl) 
  }
  case class GetWeather(input: GetWeatherInput) extends WeatherServiceOperation[GetWeatherInput, Nothing, GetWeatherOutput, Nothing, Nothing] {
    def run[F[_, _, _, _, _]](impl: WeatherServiceGen[F]): F[GetWeatherInput, Nothing, GetWeatherOutput, Nothing, Nothing] = impl.getWeather(input.location)
    def endpoint: (GetWeatherInput, smithy4s.Endpoint[WeatherServiceOperation,GetWeatherInput, Nothing, GetWeatherOutput, Nothing, Nothing]) = (input, GetWeather)
  }
  object GetWeather extends smithy4s.Endpoint[WeatherServiceOperation,GetWeatherInput, Nothing, GetWeatherOutput, Nothing, Nothing] {

    override def errorable: Option[Errorable[Nothing]] = None

    val id: ShapeId = ShapeId("weather", "GetWeather")
    val input: Schema[GetWeatherInput] = GetWeatherInput.schema.addHints(smithy4s.internals.InputOutput.Input.widen)
    val output: Schema[GetWeatherOutput] = GetWeatherOutput.schema.addHints(smithy4s.internals.InputOutput.Output.widen)
    val streamedInput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val streamedOutput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val hints: Hints = Hints(
      smithy.api.Http(method = smithy.api.NonEmptyString("GET"), uri = smithy.api.NonEmptyString("/weather/{location}"), code = 200),
      smithy.api.Documentation("Get the weather for a city"),
      smithy.api.Readonly(),
    )
    def wrap(input: GetWeatherInput) = GetWeather(input)
  }
}
