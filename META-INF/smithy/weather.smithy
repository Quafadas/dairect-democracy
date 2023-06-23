$version: "2"

namespace weather

use alloy#simpleRestJson

@simpleRestJson
service WeatherService {
    operations: [GetWeather]
}

@readonly
@http(method: "GET", uri: "/weather/{location}")
@documentation("Get the weather for a city")
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

// structure Dog {
//     @required
//     name: String
// }