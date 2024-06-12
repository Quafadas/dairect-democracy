# Goal

Allow an LLM to harness smithy services.

# Demo

You'll need an environment variable in scope called `OPEN_AI_API_TOKEN` with a valid key.

`export OPEN_AI_API_TOKEN=sk-...`

Then run:

`just demo`

OR

`scala-cli run . --main-class smithyOpenAI.Showcase`

Here's what I see.

> ChatResponse(chatcmpl-9ZLiJGaAQcj8d3wNbfyNtDMXHBS8B,1718211975,gpt-3.5-turbo-0613,List(AiChoice(AiAnswer(assistant,Some(I have created a temporary directory with the prefix `bob`. The path to the directory is `/var/folders/b7/r2s8sm653rj8w9krmxd2748w0000gn/T/bob13817770076331398513`.),None),Some(stop))))

# TO DO

- Currently, we assume that the function call succeeds. Add error handling.
- Currently, we assume that one function call is made, or that all function calls are made in parallel. Should use fs2 and `unfold` to allow bot to make calls in sequence as part of a chain of thought.

# Applications

Seem to be potentially quite wide.
- Driving my corporate infrastructure
- The BSP server, I believe is expressed in smithy. Could be used to generate a very personal dev-bot?

