package weather

// Generate this from `smithyGPTool`
val toolInfo = ujson.read("""[
        {
            "name": "get_current_weather",
            "description": "Get the current weather in a given location",
            "parameters": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "The city and state, e.g. San Francisco, CA"
                    },
                    "unit": {"type": "string", "enum": ["celsius", "fahrenheit"]}
                },
                "required": ["location"]
            }
        }
    ]""")

val smithyWeatherInfo = ujson.read("""[
        {
            "name": "GetWeather",
            "description": "Get the current weather in a given location",
            "parameters":{
              "type" : "object",
              "properties" : {
                "location" : {
                  "type" : "string",
                  "description" : "The name of the city"
                }
              }
            }
            
        }
    ]""")

val messages = ujson.Arr(
  ujson.Obj(
    "role" -> "system",
    "content" -> "You are a helpful assistent. "
  ),
  ujson.Obj(
    "role" -> "user",
    "content" -> "Tell me the forecast for today, I'm in Zurich switzerland."
  )
)

val basicBody = ujson.Obj(
  "messages" -> messages,
  "model" -> "gpt-3.5-turbo-0613",
  "temperature" -> 0,
  "functions" -> smithyWeatherInfo
)
