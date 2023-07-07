# Goal

Turn _every_ @simpleRestJson service defined in smithy, into a tool which can be called by the function call interface of the chat endpoint of openAI (integration with chatGPT, if you will).


## Sketch

1. Generate a JSON schema for the smithy operations and shapes that openAI can consume
2. Write a dispatcher, which takes the openAI response, requesting the function call, and call the appropriate smithy operation.

## Smithy gen
To generate the weather smithy model, run the following command:
```cs launch --channel https://disneystreaming.github.io/coursier.json smithy4s:0.18.0-528-d32916a -- generate src/smithy/weather/weather.smithy```

```cs launch --channel https://disneystreaming.github.io/coursier.json smithy4s:0.18.0-528-d32916a -- generate src/smithy/people/people.smithy```

```smithytranslate openapi-to-smithy --input /Users/simon/Code/smithy-call-tool/src/main/scala/openAI/openAI.openAPI.yaml /Users/simon/Code/smithy-call-tool/src/main/scala/openAI```

```cs launch --channel https://disneystreaming.github.io/coursier.json smithy4s:0.18.0-528-d32916a -- generate src/main/scala/openAI/openAI/openAPI.smithy```

## Status

A suite of unit tests which check equivalence of simple shapes against the AWS library that does the same.

I have a POC of the "end to end" concept, which
`scala-cli run .` is `callOpenAI.scala`

1. generates JSON schema for a simple case
2. sends a request to the openAI chat endpoint
3. parses the response and checks it's asking for a function call
4. calls the function, and returns the result to openAI

## Potential Applications

In combination with `fs2.unfold` and the http4s logging middleware, one gets a significant amount of langchain-esque functionality.

Not only do you get that free, but your entire api surface, that is defined in smithy, is now available to chatGPT should you wish that.

## Questions

JsonProtocolF.scala is a copy paste excercise from the smithy4s library. This is bad. Is there a world in which these methods enter smithys public API?

The original JSON schema generator sketch, looked like it was going a fairly functional route. I have not followed it faithfully. I think the current strategy can work, but it may not be acceptable for you. Is this / how big is this problem potentially?
