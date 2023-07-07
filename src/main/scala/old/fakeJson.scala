//package weather

// Generate this from `smithyGPTool` - Done!

// val toolInfo = ujson.read("""[
//         {
//             "name": "get_current_weather",
//             "description": "Get the current weather in a given location",
//             "parameters": {
//                 "type": "object",
//                 "properties": {
//                     "location": {
//                         "type": "string",
//                         "description": "The city and state, e.g. San Francisco, CA"
//                     },
//                     "unit": {"type": "string", "enum": ["celsius", "fahrenheit"]}
//                 },
//                 "required": ["location"]
//             }
//         }
//     ]""")

// val smithyWeatherInfo = ujson.read("""[
//         {
//             "name": "GetWeather",
//             "description": "Get the current weather in a given location",
//             "parameters":{
//               "type" : "object",
//               "properties" : {
//                 "location" : {
//                   "type" : "string",
//                   "description" : "The name of the city"
//                 }
//               }
//             }

//         }
//     ]""")

// val basicBody = ujson.Obj(
//   "messages" -> messages,
//   "model" -> "gpt-3.5-turbo-0613",
//   "temperature" -> 0,
//   "functions" -> smithyWeatherInfo
// )
