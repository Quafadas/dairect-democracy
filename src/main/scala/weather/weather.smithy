$version: "2"

namespace weather

use alloy#simpleRestJson
use smithy4s.meta#packedInputs

@simpleRestJson
service WeatherService {
    operations: [
      GetWeather,
      GetWeatherLatLong,
      GetWeatherLatLongPacked
    ]
}

@documentation("Get the weather for a city")
@readonly
@http(method: "GET", uri: "/weather/{location}")
operation GetWeather {
    input := {
        @httpLabel
        @required
        @documentation("The name of the city")
        location: String
    }
    output := {
        @required
        @documentation("A description of the weather in the city")
        weather: String
    }
}

@documentation("Get the weather for a city given a latitude and longitude")
@readonly
@http(method: "GET", uri: "/weather/{lat}/{long}", code: 200)
operation GetWeatherLatLong {
  input: LatLong
  output: WeatherOut
}

@documentation("Get the weather for a city given a latitude and longitude, but pack the inputs together")
@readonly
@packedInputs
@http(method: "GET", uri: "/weatherPacked/{lat}/{long}", code: 200)
operation GetWeatherLatLongPacked  {

  input: LatLong
  output: WeatherOut
}

structure LatLong {
    @documentation("Latitude") @httpLabel @required lat: Double,
    @documentation("Longditude") @httpLabel @required long: Double
}

structure WeatherOut {
    @required weather: String
}

