package weather

import cats.effect.*

object weatherServiceImpl extends WeatherService[IO]:
  def getWeather(city: String): IO[GetWeatherOutput] = IO.pure(GetWeatherOutput("lovely"))
end weatherServiceImpl

object SimpleWeatherService extends WeatherService[cats.Id]:
  def getWeather(city: String): GetWeatherOutput = GetWeatherOutput("lovely")
end SimpleWeatherService