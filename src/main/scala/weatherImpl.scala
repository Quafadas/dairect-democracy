package weather

import cats.effect.*


object weatherServiceImpl extends WeatherService[IO]:
  def getWeather(location: String): IO[GetWeatherOutput] = IO.pure(GetWeatherOutput("lovely"))
end weatherServiceImpl

object SimpleWeatherService extends WeatherService[cats.Id]:
  def getWeather(location: String): GetWeatherOutput = GetWeatherOutput("lovely")
end SimpleWeatherService