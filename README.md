# Goal

Turn _every_ @simpleRestJson service defined in smithy, into a tool which can be called by the function call interface of the chat endpoint of openAI (integration with chatGPT, if you will).


## Sketch

1. Generate a JSON schema for the smithy operations and shapes that openAI can consume
2. Write a dispatcher, which takes the openAI response, requesting the function call, and call the appropriate smithy operation.


## Status

A suite of unit tests which check equivalence of simple shapes against the AWS library that does the same.

I have a POC of the "end to end" concept, which
`scala-cli run .` is `callViaSmithy.scala`

1. Uses a smithy model of the openAI Api.
2. sends a request to the openAI chat endpoint
3. parses the response and checks it's asking for a function call
4. calls the function, and returns the result to openAI

Given src/main/scala/weather/weather.smithy, have a look at the implementation in src/main/scala/weatherImpl.scala.

The first request to openAPI looks like

```

{
  "model": "gpt-3.5-turbo-0613",
  "messages": [
    { "role": "system", "content": "You are a helpful assistent." },
    { "role": "user", "content": "Get the weather for Zurich, Switzerland" }
  ],
  "functions": [
    {
      "name": "GetWeather",
      "parameters": {
        "type": "object",
        "properties": {
          "location": {
            "description": "The name of the city",
            "type": "string"
          }
        },
        "required": ["location"]
      },
      "description": "Get the weather for a city"
    },
    {
      "name": "GetWeatherLatLong",
      "parameters": {
        "type": "object",
        "properties": {
          "lat": { "description": "Latitude", "type": "number" },
          "long": { "description": "Longditude", "type": "number" }
        },
        "required": ["lat", "long"]
      },
      "description": "Get the weather for a city given a latitude and longitude"
    },
    {
      "name": "GetWeatherLatLongPacked",
      "parameters": {
        "type": "object",
        "properties": {
          "lat": { "description": "Latitude", "type": "number" },
          "long": { "description": "Longditude", "type": "number" }
        },
        "required": ["lat", "long"]
      },
      "description": "Get the weather for a city given a latitude and longitude, but pack the inputs together"
    }
  ],
  "temperature": 0.0
}

```

The final request to openAI looks like

```
{
  "model": "gpt-3.5-turbo-0613",
  "messages": [
    { "role": "system", "content": "You are a helpful assistent." },
    { "role": "user", "content": "Get the weather for Zurich, Switzerland" },
    {
      "role": "assistant",
      "content": "",
      "name": "GetWeather",
      "function_call": {
        "name": "GetWeather",
        "arguments": "{\n  \"location\": \"Zurich, Switzerland\"\n}"
      }
    },
    {
      "role": "function",
      "content": "{\"role\":\"function\",\"name\":\"GetWeather\",\"content\":\"{\\\"weather\\\":\\\"lovely\\\"}\"}",
      "name": "GetWeather"
    }
  ],
  "temperature": 0.0
}
```

Which I think follows the example on the openAI website.
https://openai.com/blog/function-calling-and-other-api-updates

and responds with this,
```CreateChatCompletion200(CreateChatCompletionResponse(chatcmpl-7ZfFmsJbYZ1sDnb1UqRrI2L5k7bpE,chat.completion,1688734414,gpt-3.5-turbo-0613,List(CreateChatCompletionResponseChoicesItem(Some(0),Some(ChatCompletionResponseMessage(assistant,Some(The weather in Zurich, Switzerland is lovely.),None)),Some(stop))),Some(CreateChatCompletionResponseUsage(74,9,83))))```

Importantly, I don't believe there to be anything "specific" about the weather service. It should work for anything we can generate schema for.

See
awsEquivalence.schema.test.scala

for what appears to be working right now.

![worky](/working%202023-07-07.png)


## Potential Applications

In combination with `fs2.unfold` and the http4s logging middleware, one gets a significant amount of langchain-esque functionality.

Not only do you get that free, but your entire api surface, that is defined in smithy, is now available to chatGPT should you wish that.

## Questions

JsonProtocolF.scala is a copy paste excercise from the smithy4s library. This is bad. Is there a world in which these methods enter smithys public API?

The original JSON schema generator sketch, looked like it was going a fairly functional route. I have not followed it faithfully. I think the current strategy can work, but it may not be acceptable for you. Is this / how big is this problem potentially?


## Smithy gen
To generate the weather smithy model, run the following command:
```cs launch --channel https://disneystreaming.github.io/coursier.json smithy4s:0.18.0-528-d32916a -- generate src/smithy/weather/weather.smithy```

```cs launch --channel https://disneystreaming.github.io/coursier.json smithy4s:0.18.0-528-d32916a -- generate src/smithy/people/people.smithy```

```smithytranslate openapi-to-smithy --input /Users/simon/Code/smithy-call-tool/src/main/scala/openAI/openAI.openAPI.yaml /Users/simon/Code/smithy-call-tool/src/main/scala/openAI```

```cs launch --channel https://disneystreaming.github.io/coursier.json smithy4s:0.18.0-528-d32916a -- generate src/main/scala/openAI/openAI/openAPI.smithy```