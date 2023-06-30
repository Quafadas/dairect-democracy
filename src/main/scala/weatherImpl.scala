package weather

import cats.effect.*


object weatherServiceImpl extends WeatherService[IO]:


  override def getWeatherLatLongPacked(input: LatLong): IO[WeatherOut] = IO.pure(WeatherOut("packed clouds"))

  override def getWeatherLatLong(lat: Double, long: Double)  = IO.pure(WeatherOut("cloudy"))
  def getWeather(location: String): IO[GetWeatherOutput] = IO.pure(GetWeatherOutput("lovely"))

end weatherServiceImpl

object SimpleWeatherService extends WeatherService[cats.Id]:

  override def getWeatherLatLong(lat: Double, long: Double)  = WeatherOut("lovely")

  override def getWeatherLatLongPacked(input: LatLong) = WeatherOut("lovely")

  def getWeather(location: String): GetWeatherOutput = GetWeatherOutput("lovely")
end SimpleWeatherService