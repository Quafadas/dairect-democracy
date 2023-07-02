$version: "2.0"

namespace openAI.openAPI

use alloy#dataExamples
use alloy#untagged
use alloy.openapi#openapiExtensions
use error#CreateChatCompletionRequestModel
use error#CreateCompletionRequestModel
use error#CreateEditRequestModel
use error#CreateEmbeddingRequestModel
use error#CreateFineTuneRequestModel
use error#CreateModerationRequestModel
use error#CreateTranscriptionRequestModel
use error#CreateTranslationRequestModel
use smithytranslate#contentType

@openapiExtensions(
    "x-oaiMeta": {
        groups: [
            {
                description: "List and describe the various models available in the API. You can refer to the [Models](/docs/models) documentation to understand what models are available and the differences between them.\n"
                id: "models"
                title: "Models"
            }
            {
                description: "Given a list of messages comprising a conversation, the model will return a response.\n"
                id: "chat"
                title: "Chat"
            }
            {
                description: "Given a prompt, the model will return one or more predicted completions, and can also return the probabilities of alternative tokens at each position.\n"
                id: "completions"
                title: "Completions"
            }
            {
                description: "Given a prompt and an instruction, the model will return an edited version of the prompt.\n"
                id: "edits"
                title: "Edits"
            }
            {
                description: "Given a prompt and/or an input image, the model will generate a new image.\n\nRelated guide: [Image generation](/docs/guides/images)\n"
                id: "images"
                title: "Images"
            }
            {
                description: "Get a vector representation of a given input that can be easily consumed by machine learning models and algorithms.\n\nRelated guide: [Embeddings](/docs/guides/embeddings)\n"
                id: "embeddings"
                title: "Embeddings"
            }
            {
                description: "Learn how to turn audio into text.\n\nRelated guide: [Speech to text](/docs/guides/speech-to-text)\n"
                id: "audio"
                title: "Audio"
            }
            {
                description: "Files are used to upload documents that can be used with features like [Fine-tuning](/docs/api-reference/fine-tunes).\n"
                id: "files"
                title: "Files"
            }
            {
                description: "Manage fine-tuning jobs to tailor a model to your specific training data.\n\nRelated guide: [Fine-tune models](/docs/guides/fine-tuning)\n"
                id: "fine-tunes"
                title: "Fine-tunes"
            }
            {
                description: "Given a input text, outputs if the model classifies it as violating OpenAI's content policy.\n\nRelated guide: [Moderations](/docs/guides/moderation)\n"
                id: "moderations"
                title: "Moderations"
            }
        ]
    }
)
service OpenAIOpenAPIService {
    operations: [
        CancelFineTune
        CreateChatCompletion
        CreateCompletion
        CreateEdit
        CreateEmbedding
        CreateFile
        CreateFineTune
        CreateImage
        CreateImageEdit
        CreateImageVariation
        CreateModeration
        CreateTranscription
        CreateTranslation
        DeleteFile
        DeleteModel
        DownloadFile
        ListFiles
        ListFineTuneEvents
        ListFineTunes
        ListModels
        RetrieveFile
        RetrieveFineTune
        RetrieveModel
    ]
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Cancel fine-tune"
        response: "{\n  \"id\": \"ft-xhrpBbvVUzYGo8oUO1FY4nI7\",\n  \"object\": \"fine-tune\",\n  \"model\": \"curie\",\n  \"created_at\": 1614807770,\n  \"events\": [ { ... } ],\n  \"fine_tuned_model\": null,\n  \"hyperparams\": { ... },\n  \"organization_id\": \"org-...\",\n  \"result_files\": [],\n  \"status\": \"cancelled\",\n  \"validation_files\": [],\n  \"training_files\": [\n    {\n      \"id\": \"file-XGinujblHPwGLSztz8cPS8XY\",\n      \"object\": \"file\",\n      \"bytes\": 1547276,\n      \"created_at\": 1610062281,\n      \"filename\": \"my-data-train.jsonl\",\n      \"purpose\": \"fine-tune-train\"\n    }\n  ],\n  \"updated_at\": 1614807789,\n}\n"
        path: "cancel"
        group: "fine-tunes"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.cancelFineTune(\"ft-AF1WoRqd3aJAHsqc9NY7iL8F\");\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.FineTune.cancel(id=\"ft-AF1WoRqd3aJAHsqc9NY7iL8F\")\n"
            curl: "curl https://api.openai.com/v1/fine-tunes/ft-AF1WoRqd3aJAHsqc9NY7iL8F/cancel \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\"\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/fine-tunes/{fine_tune_id}/cancel"
    code: 200
)
operation CancelFineTune {
    input: CancelFineTuneInput
    output: CancelFineTune200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create chat completion"
        response: "{\n  \"id\": \"chatcmpl-123\",\n  \"object\": \"chat.completion\",\n  \"created\": 1677652288,\n  \"choices\": [{\n    \"index\": 0,\n    \"message\": {\n      \"role\": \"assistant\",\n      \"content\": \"\\n\\nHello there, how may I assist you today?\",\n    },\n    \"finish_reason\": \"stop\"\n  }],\n  \"usage\": {\n    \"prompt_tokens\": 9,\n    \"completion_tokens\": 12,\n    \"total_tokens\": 21\n  }\n}\n"
        beta: true
        path: "create"
        group: "chat"
        parameters: "{\n  \"model\": \"gpt-3.5-turbo\",\n  \"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"user\", \"content\": \"Hello!\"}]\n}\n"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\n\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\n\nconst completion = await openai.createChatCompletion({\n  model: \"gpt-3.5-turbo\",\n  messages: [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {role: \"user\", content: \"Hello world\"}],\n});\nconsole.log(completion.data.choices[0].message);\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\n\ncompletion = openai.ChatCompletion.create(\n  model=\"gpt-3.5-turbo\",\n  messages=[\n    {\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},\n    {\"role\": \"user\", \"content\": \"Hello!\"}\n  ]\n)\n\nprint(completion.choices[0].message)\n"
            curl: "curl https://api.openai.com/v1/chat/completions \\\n  -H \"Content-Type: application/json\" \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -d '{\n    \"model\": \"gpt-3.5-turbo\",\n    \"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"user\", \"content\": \"Hello!\"}]\n  }'\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/chat/completions"
    code: 200
)
operation CreateChatCompletion {
    input: CreateChatCompletionInput
    output: CreateChatCompletion200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create completion"
        response: "{\n  \"id\": \"cmpl-uqkvlQyYK7bGYrRHQ0eXlWi7\",\n  \"object\": \"text_completion\",\n  \"created\": 1589478378,\n  \"model\": \"VAR_model_id\",\n  \"choices\": [\n    {\n      \"text\": \"\\n\\nThis is indeed a test\",\n      \"index\": 0,\n      \"logprobs\": null,\n      \"finish_reason\": \"length\"\n    }\n  ],\n  \"usage\": {\n    \"prompt_tokens\": 5,\n    \"completion_tokens\": 7,\n    \"total_tokens\": 12\n  }\n}\n"
        path: "create"
        group: "completions"
        parameters: "{\n  \"model\": \"VAR_model_id\",\n  \"prompt\": \"Say this is a test\",\n  \"max_tokens\": 7,\n  \"temperature\": 0,\n  \"top_p\": 1,\n  \"n\": 1,\n  \"stream\": false,\n  \"logprobs\": null,\n  \"stop\": \"\\n\"\n}\n"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.createCompletion({\n  model: \"VAR_model_id\",\n  prompt: \"Say this is a test\",\n  max_tokens: 7,\n  temperature: 0,\n});\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.Completion.create(\n  model=\"VAR_model_id\",\n  prompt=\"Say this is a test\",\n  max_tokens=7,\n  temperature=0\n)\n"
            curl: "curl https://api.openai.com/v1/completions \\\n  -H \"Content-Type: application/json\" \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -d '{\n    \"model\": \"VAR_model_id\",\n    \"prompt\": \"Say this is a test\",\n    \"max_tokens\": 7,\n    \"temperature\": 0\n  }'\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/completions"
    code: 200
)
operation CreateCompletion {
    input: CreateCompletionInput
    output: CreateCompletion200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create edit"
        response: "{\n  \"object\": \"edit\",\n  \"created\": 1589478378,\n  \"choices\": [\n    {\n      \"text\": \"What day of the week is it?\",\n      \"index\": 0,\n    }\n  ],\n  \"usage\": {\n    \"prompt_tokens\": 25,\n    \"completion_tokens\": 32,\n    \"total_tokens\": 57\n  }\n}\n"
        path: "create"
        group: "edits"
        parameters: "{\n  \"model\": \"VAR_model_id\",\n  \"input\": \"What day of the wek is it?\",\n  \"instruction\": \"Fix the spelling mistakes\"\n}\n"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.createEdit({\n  model: \"VAR_model_id\",\n  input: \"What day of the wek is it?\",\n  instruction: \"Fix the spelling mistakes\",\n});\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.Edit.create(\n  model=\"VAR_model_id\",\n  input=\"What day of the wek is it?\",\n  instruction=\"Fix the spelling mistakes\"\n)\n"
            curl: "curl https://api.openai.com/v1/edits \\\n  -H \"Content-Type: application/json\" \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -d '{\n    \"model\": \"VAR_model_id\",\n    \"input\": \"What day of the wek is it?\",\n    \"instruction\": \"Fix the spelling mistakes\"\n  }'\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/edits"
    code: 200
)
operation CreateEdit {
    input: CreateEditInput
    output: CreateEdit200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create embeddings"
        response: "{\n  \"object\": \"list\",\n  \"data\": [\n    {\n      \"object\": \"embedding\",\n      \"embedding\": [\n        0.0023064255,\n        -0.009327292,\n        .... (1536 floats total for ada-002)\n        -0.0028842222,\n      ],\n      \"index\": 0\n    }\n  ],\n  \"model\": \"text-embedding-ada-002\",\n  \"usage\": {\n    \"prompt_tokens\": 8,\n    \"total_tokens\": 8\n  }\n}\n"
        path: "create"
        group: "embeddings"
        parameters: "{\n  \"model\": \"text-embedding-ada-002\",\n  \"input\": \"The food was delicious and the waiter...\"\n}\n"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.createEmbedding({\n  model: \"text-embedding-ada-002\",\n  input: \"The food was delicious and the waiter...\",\n});\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.Embedding.create(\n  model=\"text-embedding-ada-002\",\n  input=\"The food was delicious and the waiter...\"\n)\n"
            curl: "curl https://api.openai.com/v1/embeddings \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -H \"Content-Type: application/json\" \\\n  -d '{\n    \"input\": \"The food was delicious and the waiter...\",\n    \"model\": \"text-embedding-ada-002\"\n  }'\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/embeddings"
    code: 200
)
operation CreateEmbedding {
    input: CreateEmbeddingInput
    output: CreateEmbedding200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Upload file"
        response: "{\n  \"id\": \"file-XjGxS3KTG0uNmNOK362iJua3\",\n  \"object\": \"file\",\n  \"bytes\": 140,\n  \"created_at\": 1613779121,\n  \"filename\": \"mydata.jsonl\",\n  \"purpose\": \"fine-tune\"\n}\n"
        path: "upload"
        group: "files"
        examples: {
            "node.js": "const fs = require(\"fs\");\nconst { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.createFile(\n  fs.createReadStream(\"mydata.jsonl\"),\n  \"fine-tune\"\n);\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.File.create(\n  file=open(\"mydata.jsonl\", \"rb\"),\n  purpose='fine-tune'\n)\n"
            curl: "curl https://api.openai.com/v1/files \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -F purpose=\"fine-tune\" \\\n  -F file=\"@mydata.jsonl\"\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/files"
    code: 200
)
operation CreateFile {
    input: CreateFileInput
    output: CreateFile200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create fine-tune"
        response: "{\n  \"id\": \"ft-AF1WoRqd3aJAHsqc9NY7iL8F\",\n  \"object\": \"fine-tune\",\n  \"model\": \"curie\",\n  \"created_at\": 1614807352,\n  \"events\": [\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807352,\n      \"level\": \"info\",\n      \"message\": \"Job enqueued. Waiting for jobs ahead to complete. Queue number: 0.\"\n    }\n  ],\n  \"fine_tuned_model\": null,\n  \"hyperparams\": {\n    \"batch_size\": 4,\n    \"learning_rate_multiplier\": 0.1,\n    \"n_epochs\": 4,\n    \"prompt_loss_weight\": 0.1,\n  },\n  \"organization_id\": \"org-...\",\n  \"result_files\": [],\n  \"status\": \"pending\",\n  \"validation_files\": [],\n  \"training_files\": [\n    {\n      \"id\": \"file-XGinujblHPwGLSztz8cPS8XY\",\n      \"object\": \"file\",\n      \"bytes\": 1547276,\n      \"created_at\": 1610062281,\n      \"filename\": \"my-data-train.jsonl\",\n      \"purpose\": \"fine-tune-train\"\n    }\n  ],\n  \"updated_at\": 1614807352,\n}\n"
        path: "create"
        group: "fine-tunes"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.createFineTune({\n  training_file: \"file-XGinujblHPwGLSztz8cPS8XY\",\n});\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.FineTune.create(training_file=\"file-XGinujblHPwGLSztz8cPS8XY\")\n"
            curl: "curl https://api.openai.com/v1/fine-tunes \\\n  -H \"Content-Type: application/json\" \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -d '{\n    \"training_file\": \"file-XGinujblHPwGLSztz8cPS8XY\"\n  }'\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/fine-tunes"
    code: 200
)
operation CreateFineTune {
    input: CreateFineTuneInput
    output: CreateFineTune200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create image"
        response: "{\n  \"created\": 1589478378,\n  \"data\": [\n    {\n      \"url\": \"https://...\"\n    },\n    {\n      \"url\": \"https://...\"\n    }\n  ]\n}\n"
        beta: true
        path: "create"
        group: "images"
        parameters: "{\n  \"prompt\": \"A cute baby sea otter\",\n  \"n\": 2,\n  \"size\": \"1024x1024\"\n}\n"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.createImage({\n  prompt: \"A cute baby sea otter\",\n  n: 2,\n  size: \"1024x1024\",\n});\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.Image.create(\n  prompt=\"A cute baby sea otter\",\n  n=2,\n  size=\"1024x1024\"\n)\n"
            curl: "curl https://api.openai.com/v1/images/generations \\\n  -H \"Content-Type: application/json\" \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -d '{\n    \"prompt\": \"A cute baby sea otter\",\n    \"n\": 2,\n    \"size\": \"1024x1024\"\n  }'\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/images/generations"
    code: 200
)
operation CreateImage {
    input: CreateImageInput
    output: CreateImage200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create image edit"
        response: "{\n  \"created\": 1589478378,\n  \"data\": [\n    {\n      \"url\": \"https://...\"\n    },\n    {\n      \"url\": \"https://...\"\n    }\n  ]\n}\n"
        beta: true
        path: "create-edit"
        group: "images"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.createImageEdit(\n  fs.createReadStream(\"otter.png\"),\n  fs.createReadStream(\"mask.png\"),\n  \"A cute baby sea otter wearing a beret\",\n  2,\n  \"1024x1024\"\n);\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.Image.create_edit(\n  image=open(\"otter.png\", \"rb\"),\n  mask=open(\"mask.png\", \"rb\"),\n  prompt=\"A cute baby sea otter wearing a beret\",\n  n=2,\n  size=\"1024x1024\"\n)\n"
            curl: "curl https://api.openai.com/v1/images/edits \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -F image=\"@otter.png\" \\\n  -F mask=\"@mask.png\" \\\n  -F prompt=\"A cute baby sea otter wearing a beret\" \\\n  -F n=2 \\\n  -F size=\"1024x1024\"\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/images/edits"
    code: 200
)
operation CreateImageEdit {
    input: CreateImageEditInput
    output: CreateImageEdit200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create image variation"
        response: "{\n  \"created\": 1589478378,\n  \"data\": [\n    {\n      \"url\": \"https://...\"\n    },\n    {\n      \"url\": \"https://...\"\n    }\n  ]\n}\n"
        beta: true
        path: "create-variation"
        group: "images"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.createImageVariation(\n  fs.createReadStream(\"otter.png\"),\n  2,\n  \"1024x1024\"\n);\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.Image.create_variation(\n  image=open(\"otter.png\", \"rb\"),\n  n=2,\n  size=\"1024x1024\"\n)\n"
            curl: "curl https://api.openai.com/v1/images/variations \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -F image=\"@otter.png\" \\\n  -F n=2 \\\n  -F size=\"1024x1024\"\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/images/variations"
    code: 200
)
operation CreateImageVariation {
    input: CreateImageVariationInput
    output: CreateImageVariation200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create moderation"
        response: "{\n  \"id\": \"modr-5MWoLO\",\n  \"model\": \"text-moderation-001\",\n  \"results\": [\n    {\n      \"categories\": {\n        \"hate\": false,\n        \"hate/threatening\": true,\n        \"self-harm\": false,\n        \"sexual\": false,\n        \"sexual/minors\": false,\n        \"violence\": true,\n        \"violence/graphic\": false\n      },\n      \"category_scores\": {\n        \"hate\": 0.22714105248451233,\n        \"hate/threatening\": 0.4132447838783264,\n        \"self-harm\": 0.005232391878962517,\n        \"sexual\": 0.01407341007143259,\n        \"sexual/minors\": 0.0038522258400917053,\n        \"violence\": 0.9223177433013916,\n        \"violence/graphic\": 0.036865197122097015\n      },\n      \"flagged\": true\n    }\n  ]\n}\n"
        path: "create"
        group: "moderations"
        parameters: "{\n  \"input\": \"I want to kill them.\"\n}\n"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.createModeration({\n  input: \"I want to kill them.\",\n});\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.Moderation.create(\n  input=\"I want to kill them.\",\n)\n"
            curl: "curl https://api.openai.com/v1/moderations \\\n  -H \"Content-Type: application/json\" \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -d '{\n    \"input\": \"I want to kill them.\"\n  }'\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/moderations"
    code: 200
)
operation CreateModeration {
    input: CreateModerationInput
    output: CreateModeration200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create transcription"
        response: "{\n  \"text\": \"Imagine the wildest idea that you've ever had, and you're curious about how it might scale to something that's a 100, a 1,000 times bigger. This is a place where you can get to do that.\"\n}\n"
        beta: true
        path: "create"
        group: "audio"
        parameters: "{\n  \"file\": \"audio.mp3\",\n  \"model\": \"whisper-1\"\n}\n"
        examples: {
            node: "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst resp = await openai.createTranscription(\n  fs.createReadStream(\"audio.mp3\"),\n  \"whisper-1\"\n);\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\naudio_file = open(\"audio.mp3\", \"rb\")\ntranscript = openai.Audio.transcribe(\"whisper-1\", audio_file)\n"
            curl: "curl https://api.openai.com/v1/audio/transcriptions \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -H \"Content-Type: multipart/form-data\" \\\n  -F file=\"@/path/to/file/audio.mp3\" \\\n  -F model=\"whisper-1\"\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/audio/transcriptions"
    code: 200
)
operation CreateTranscription {
    input: CreateTranscriptionInput
    output: CreateTranscription200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Create translation"
        response: "{\n  \"text\": \"Hello, my name is Wolfgang and I come from Germany. Where are you heading today?\"\n}\n"
        beta: true
        path: "create"
        group: "audio"
        parameters: "{\n  \"file\": \"german.m4a\",\n  \"model\": \"whisper-1\"\n}\n"
        examples: {
            node: "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst resp = await openai.createTranslation(\n  fs.createReadStream(\"audio.mp3\"),\n  \"whisper-1\"\n);\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\naudio_file = open(\"german.m4a\", \"rb\")\ntranscript = openai.Audio.translate(\"whisper-1\", audio_file)\n"
            curl: "curl https://api.openai.com/v1/audio/translations \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" \\\n  -H \"Content-Type: multipart/form-data\" \\\n  -F file=\"@/path/to/file/german.m4a\" \\\n  -F model=\"whisper-1\"\n"
        }
    }
)
@http(
    method: "POST"
    uri: "/audio/translations"
    code: 200
)
operation CreateTranslation {
    input: CreateTranslationInput
    output: CreateTranslation200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Delete file"
        response: "{\n  \"id\": \"file-XjGxS3KTG0uNmNOK362iJua3\",\n  \"object\": \"file\",\n  \"deleted\": true\n}\n"
        path: "delete"
        group: "files"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.deleteFile(\"file-XjGxS3KTG0uNmNOK362iJua3\");\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.File.delete(\"file-XjGxS3KTG0uNmNOK362iJua3\")\n"
            curl: "curl https://api.openai.com/v1/files/file-XjGxS3KTG0uNmNOK362iJua3 \\\n  -X DELETE \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\"\n"
        }
    }
)
@http(
    method: "DELETE"
    uri: "/files/{file_id}"
    code: 200
)
operation DeleteFile {
    input: DeleteFileInput
    output: DeleteFile200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Delete fine-tune model"
        response: "{\n  \"id\": \"curie:ft-acmeco-2021-03-03-21-44-20\",\n  \"object\": \"model\",\n  \"deleted\": true\n}\n"
        path: "delete-model"
        group: "fine-tunes"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.deleteModel('curie:ft-acmeco-2021-03-03-21-44-20');\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.Model.delete(\"curie:ft-acmeco-2021-03-03-21-44-20\")\n"
            curl: "curl https://api.openai.com/v1/models/curie:ft-acmeco-2021-03-03-21-44-20 \\\n  -X DELETE \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\"\n"
        }
    }
)
@http(
    method: "DELETE"
    uri: "/models/{model}"
    code: 200
)
operation DeleteModel {
    input: DeleteModelInput
    output: DeleteModel200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Retrieve file content"
        path: "retrieve-content"
        group: "files"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.downloadFile(\"file-XjGxS3KTG0uNmNOK362iJua3\");\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\ncontent = openai.File.download(\"file-XjGxS3KTG0uNmNOK362iJua3\")\n"
            curl: "curl https://api.openai.com/v1/files/file-XjGxS3KTG0uNmNOK362iJua3/content \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\" > file.jsonl\n"
        }
    }
)
@http(
    method: "GET"
    uri: "/files/{file_id}/content"
    code: 200
)
operation DownloadFile {
    input: DownloadFileInput
    output: DownloadFile200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "List files"
        response: "{\n  \"data\": [\n    {\n      \"id\": \"file-ccdDZrC3iZVNiQVeEA6Z66wf\",\n      \"object\": \"file\",\n      \"bytes\": 175,\n      \"created_at\": 1613677385,\n      \"filename\": \"train.jsonl\",\n      \"purpose\": \"search\"\n    },\n    {\n      \"id\": \"file-XjGxS3KTG0uNmNOK362iJua3\",\n      \"object\": \"file\",\n      \"bytes\": 140,\n      \"created_at\": 1613779121,\n      \"filename\": \"puppy.jsonl\",\n      \"purpose\": \"search\"\n    }\n  ],\n  \"object\": \"list\"\n}\n"
        path: "list"
        group: "files"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.listFiles();\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.File.list()\n"
            curl: "curl https://api.openai.com/v1/files \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\"\n"
        }
    }
)
@http(
    method: "GET"
    uri: "/files"
    code: 200
)
operation ListFiles {
    input: Unit
    output: ListFiles200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "List fine-tune events"
        response: "{\n  \"object\": \"list\",\n  \"data\": [\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807352,\n      \"level\": \"info\",\n      \"message\": \"Job enqueued. Waiting for jobs ahead to complete. Queue number: 0.\"\n    },\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807356,\n      \"level\": \"info\",\n      \"message\": \"Job started.\"\n    },\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807861,\n      \"level\": \"info\",\n      \"message\": \"Uploaded snapshot: curie:ft-acmeco-2021-03-03-21-44-20.\"\n    },\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807864,\n      \"level\": \"info\",\n      \"message\": \"Uploaded result files: file-QQm6ZpqdNwAaVC3aSz5sWwLT.\"\n    },\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807864,\n      \"level\": \"info\",\n      \"message\": \"Job succeeded.\"\n    }\n  ]\n}\n"
        path: "events"
        group: "fine-tunes"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.listFineTuneEvents(\"ft-AF1WoRqd3aJAHsqc9NY7iL8F\");\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.FineTune.list_events(id=\"ft-AF1WoRqd3aJAHsqc9NY7iL8F\")\n"
            curl: "curl https://api.openai.com/v1/fine-tunes/ft-AF1WoRqd3aJAHsqc9NY7iL8F/events \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\"\n"
        }
    }
)
@http(
    method: "GET"
    uri: "/fine-tunes/{fine_tune_id}/events"
    code: 200
)
operation ListFineTuneEvents {
    input: ListFineTuneEventsInput
    output: ListFineTuneEvents200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "List fine-tunes"
        response: "{\n  \"object\": \"list\",\n  \"data\": [\n    {\n      \"id\": \"ft-AF1WoRqd3aJAHsqc9NY7iL8F\",\n      \"object\": \"fine-tune\",\n      \"model\": \"curie\",\n      \"created_at\": 1614807352,\n      \"fine_tuned_model\": null,\n      \"hyperparams\": { ... },\n      \"organization_id\": \"org-...\",\n      \"result_files\": [],\n      \"status\": \"pending\",\n      \"validation_files\": [],\n      \"training_files\": [ { ... } ],\n      \"updated_at\": 1614807352,\n    },\n    { ... },\n    { ... }\n  ]\n}\n"
        path: "list"
        group: "fine-tunes"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.listFineTunes();\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.FineTune.list()\n"
            curl: "curl https://api.openai.com/v1/fine-tunes \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\"\n"
        }
    }
)
@http(
    method: "GET"
    uri: "/fine-tunes"
    code: 200
)
operation ListFineTunes {
    input: Unit
    output: ListFineTunes200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "List models"
        response: "{\n  \"data\": [\n    {\n      \"id\": \"model-id-0\",\n      \"object\": \"model\",\n      \"owned_by\": \"organization-owner\",\n      \"permission\": [...]\n    },\n    {\n      \"id\": \"model-id-1\",\n      \"object\": \"model\",\n      \"owned_by\": \"organization-owner\",\n      \"permission\": [...]\n    },\n    {\n      \"id\": \"model-id-2\",\n      \"object\": \"model\",\n      \"owned_by\": \"openai\",\n      \"permission\": [...]\n    },\n  ],\n  \"object\": \"list\"\n}\n"
        path: "list"
        group: "models"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.listModels();\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.Model.list()\n"
            curl: "curl https://api.openai.com/v1/models \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\"\n"
        }
    }
)
@http(
    method: "GET"
    uri: "/models"
    code: 200
)
operation ListModels {
    input: Unit
    output: ListModels200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Retrieve file"
        response: "{\n  \"id\": \"file-XjGxS3KTG0uNmNOK362iJua3\",\n  \"object\": \"file\",\n  \"bytes\": 140,\n  \"created_at\": 1613779657,\n  \"filename\": \"mydata.jsonl\",\n  \"purpose\": \"fine-tune\"\n}\n"
        path: "retrieve"
        group: "files"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.retrieveFile(\"file-XjGxS3KTG0uNmNOK362iJua3\");\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.File.retrieve(\"file-XjGxS3KTG0uNmNOK362iJua3\")\n"
            curl: "curl https://api.openai.com/v1/files/file-XjGxS3KTG0uNmNOK362iJua3 \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\"\n"
        }
    }
)
@http(
    method: "GET"
    uri: "/files/{file_id}"
    code: 200
)
operation RetrieveFile {
    input: RetrieveFileInput
    output: RetrieveFile200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Retrieve fine-tune"
        response: "{\n  \"id\": \"ft-AF1WoRqd3aJAHsqc9NY7iL8F\",\n  \"object\": \"fine-tune\",\n  \"model\": \"curie\",\n  \"created_at\": 1614807352,\n  \"events\": [\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807352,\n      \"level\": \"info\",\n      \"message\": \"Job enqueued. Waiting for jobs ahead to complete. Queue number: 0.\"\n    },\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807356,\n      \"level\": \"info\",\n      \"message\": \"Job started.\"\n    },\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807861,\n      \"level\": \"info\",\n      \"message\": \"Uploaded snapshot: curie:ft-acmeco-2021-03-03-21-44-20.\"\n    },\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807864,\n      \"level\": \"info\",\n      \"message\": \"Uploaded result files: file-QQm6ZpqdNwAaVC3aSz5sWwLT.\"\n    },\n    {\n      \"object\": \"fine-tune-event\",\n      \"created_at\": 1614807864,\n      \"level\": \"info\",\n      \"message\": \"Job succeeded.\"\n    }\n  ],\n  \"fine_tuned_model\": \"curie:ft-acmeco-2021-03-03-21-44-20\",\n  \"hyperparams\": {\n    \"batch_size\": 4,\n    \"learning_rate_multiplier\": 0.1,\n    \"n_epochs\": 4,\n    \"prompt_loss_weight\": 0.1,\n  },\n  \"organization_id\": \"org-...\",\n  \"result_files\": [\n    {\n      \"id\": \"file-QQm6ZpqdNwAaVC3aSz5sWwLT\",\n      \"object\": \"file\",\n      \"bytes\": 81509,\n      \"created_at\": 1614807863,\n      \"filename\": \"compiled_results.csv\",\n      \"purpose\": \"fine-tune-results\"\n    }\n  ],\n  \"status\": \"succeeded\",\n  \"validation_files\": [],\n  \"training_files\": [\n    {\n      \"id\": \"file-XGinujblHPwGLSztz8cPS8XY\",\n      \"object\": \"file\",\n      \"bytes\": 1547276,\n      \"created_at\": 1610062281,\n      \"filename\": \"my-data-train.jsonl\",\n      \"purpose\": \"fine-tune-train\"\n    }\n  ],\n  \"updated_at\": 1614807865,\n}\n"
        path: "retrieve"
        group: "fine-tunes"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.retrieveFineTune(\"ft-AF1WoRqd3aJAHsqc9NY7iL8F\");\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.FineTune.retrieve(id=\"ft-AF1WoRqd3aJAHsqc9NY7iL8F\")\n"
            curl: "curl https://api.openai.com/v1/fine-tunes/ft-AF1WoRqd3aJAHsqc9NY7iL8F \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\"\n"
        }
    }
)
@http(
    method: "GET"
    uri: "/fine-tunes/{fine_tune_id}"
    code: 200
)
operation RetrieveFineTune {
    input: RetrieveFineTuneInput
    output: RetrieveFineTune200
}

@openapiExtensions(
    "x-oaiMeta": {
        name: "Retrieve model"
        response: "{\n  \"id\": \"VAR_model_id\",\n  \"object\": \"model\",\n  \"owned_by\": \"openai\",\n  \"permission\": [...]\n}\n"
        path: "retrieve"
        group: "models"
        examples: {
            "node.js": "const { Configuration, OpenAIApi } = require(\"openai\");\nconst configuration = new Configuration({\n  apiKey: process.env.OPENAI_API_KEY,\n});\nconst openai = new OpenAIApi(configuration);\nconst response = await openai.retrieveModel(\"VAR_model_id\");\n"
            python: "import os\nimport openai\nopenai.api_key = os.getenv(\"OPENAI_API_KEY\")\nopenai.Model.retrieve(\"VAR_model_id\")\n"
            curl: "curl https://api.openai.com/v1/models/VAR_model_id \\\n  -H \"Authorization: Bearer $OPENAI_API_KEY\"\n"
        }
    }
)
@http(
    method: "GET"
    uri: "/models/{model}"
    code: 200
)
operation RetrieveModel {
    input: RetrieveModelInput
    output: RetrieveModel200
}

structure CancelFineTune200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: FineTune
}

structure CancelFineTuneInput {
    /// The ID of the fine-tune job to cancel
    ///
    @dataExamples([
        {
            json: "ft-AF1WoRqd3aJAHsqc9NY7iL8F"
        }
    ])
    @httpLabel
    @required
    fine_tune_id: String
}

structure Categories {
    @required
    hate: Boolean
    @jsonName("hate/threatening")
    @required
    hatethreatening: Boolean
    @jsonName("self-harm")
    @required
    self_harm: Boolean
    @required
    sexual: Boolean
    @jsonName("sexual/minors")
    @required
    sexualminors: Boolean
    @required
    violence: Boolean
    @jsonName("violence/graphic")
    @required
    violencegraphic: Boolean
}

structure CategoryScores {
    @required
    hate: Double
    @jsonName("hate/threatening")
    @required
    hatethreatening: Double
    @jsonName("self-harm")
    @required
    self_harm: Double
    @required
    sexual: Double
    @jsonName("sexual/minors")
    @required
    sexualminors: Double
    @required
    violence: Double
    @jsonName("violence/graphic")
    @required
    violencegraphic: Double
}

structure ChatCompletionFunctions {
    /// The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64.
    @required
    name: String
    /// A description of what the function does, used by the model to choose when and how to call the function.
    description: String
    @required
    parameters: ChatCompletionFunctionParameters
}

structure ChatCompletionRequestMessage {
    @required
    role: ChatCompletionRequestMessageRole
    /// The contents of the message. `content` is required for all messages, and may be null for assistant messages with function calls.
    @required
    content: String
    /// The name of the author of this message. `name` is required if role is `function`, and it should be the name of the function whose response is in the `content`. May contain a-z, A-Z, 0-9, and underscores, with a maximum length of 64 characters.
    name: String
    function_call: ChatCompletionRequestMessageFunctionCall
}

/// The name and arguments of a function that should be called, as generated by the model.
structure ChatCompletionRequestMessageFunctionCall {
    /// The name of the function to call.
    @required
    name: String
    /// The arguments to call the function with, as generated by the model in JSON format. Note that the model does not always generate valid JSON, and may hallucinate parameters not defined by your function schema. Validate the arguments in your code before calling your function.
    @required
    arguments: String
}

structure ChatCompletionResponseMessage {
    @required
    role: ChatCompletionResponseMessageRole
    /// The contents of the message.
    content: String
    function_call: ChatCompletionResponseMessageFunctionCall
}

/// The name and arguments of a function that should be called, as generated by the model.
structure ChatCompletionResponseMessageFunctionCall {
    /// The name of the function to call.
    name: String
    /// The arguments to call the function with, as generated by the model in JSON format. Note that the model does not always generate valid JSON, and may hallucinate parameters not defined by your function schema. Validate the arguments in your code before calling your function.
    arguments: String
}

structure ChatCompletionStreamResponseDelta {
    role: ChatCompletionStreamResponseDeltaRole
    /// The contents of the chunk message.
    content: String
    function_call: ChatCompletionStreamResponseDeltaFunctionCall
}

/// The name and arguments of a function that should be called, as generated by the model.
structure ChatCompletionStreamResponseDeltaFunctionCall {
    /// The name of the function to call.
    name: String
    /// The arguments to call the function with, as generated by the model in JSON format. Note that the model does not always generate valid JSON, and may hallucinate parameters not defined by your function schema. Validate the arguments in your code before calling your function.
    arguments: String
}

structure CreateChatCompletion200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateChatCompletionResponse
}

structure CreateChatCompletionInput {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateChatCompletionRequest
}

structure CreateChatCompletionRequest {
    @required
    model: CreateChatCompletionRequestModel
    @required
    messages: Messages
    functions: Functions
    function_call: CreateChatCompletionRequestFunctionCall
    /// What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.
    ///
    /// We generally recommend altering this or `top_p` but not both.
    ///
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 0
        max: 2
    )
    temperature: Double
    /// An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.
    ///
    /// We generally recommend altering this or `temperature` but not both.
    ///
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 0
        max: 1
    )
    top_p: Double
    /// How many chat completion choices to generate for each input message.
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 1
        max: 128
    )
    n: Integer
    /// If set, partial message deltas will be sent, like in ChatGPT. Tokens will be sent as data-only [server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#Event_stream_format) as they become available, with the stream terminated by a `data: [DONE]` message. [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_stream_completions.ipynb).
    ///
    stream: Boolean
    stop: CreateChatCompletionRequestStop
    /// The maximum number of [tokens](/tokenizer) to generate in the chat completion.
    ///
    /// The total length of input tokens and generated tokens is limited by the model's context length. [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_count_tokens_with_tiktoken.ipynb) for counting tokens.
    ///
    max_tokens: Integer
    /// Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far, increasing the model's likelihood to talk about new topics.
    ///
    /// [See more information about frequency and presence penalties.](/docs/api-reference/parameter-details)
    ///
    @range(
        min: -2
        max: 2
    )
    presence_penalty: Double
    /// Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far, decreasing the model's likelihood to repeat the same line verbatim.
    ///
    /// [See more information about frequency and presence penalties.](/docs/api-reference/parameter-details)
    ///
    @range(
        min: -2
        max: 2
    )
    frequency_penalty: Double
    /// Modify the likelihood of specified tokens appearing in the completion.
    ///
    /// Accepts a json object that maps tokens (specified by their token ID in the tokenizer) to an associated bias value from -100 to 100. Mathematically, the bias is added to the logits generated by the model prior to sampling. The exact effect will vary per model, but values between -1 and 1 should decrease or increase likelihood of selection; values like -100 or 100 should result in a ban or exclusive selection of the relevant token.
    ///
    @openapiExtensions(
        "x-oaiTypeLabel": "map"
    )
    logit_bias: Document
    /// A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse. [Learn more](/docs/guides/safety-best-practices/end-user-ids).
    ///
    @dataExamples([
        {
            json: "user-1234"
        }
    ])
    user: String
}

structure CreateChatCompletionRequestFunctionCallOneOfAlt1 {
    /// The name of the function to call.
    @required
    name: String
}

structure CreateChatCompletionResponse {
    @required
    id: String
    @required
    object: String
    @required
    created: Integer
    @required
    model: String
    @required
    choices: CreateChatCompletionResponseChoices
    usage: CreateChatCompletionResponseUsage
}

structure CreateChatCompletionResponseChoicesItem {
    index: Integer
    message: ChatCompletionResponseMessage
    finish_reason: CreateChatCompletionResponseChoicesItemFinishReason
}

structure CreateChatCompletionResponseUsage {
    @required
    prompt_tokens: Integer
    @required
    completion_tokens: Integer
    @required
    total_tokens: Integer
}

structure CreateChatCompletionStreamResponse {
    @required
    id: String
    @required
    object: String
    @required
    created: Integer
    @required
    model: String
    @required
    choices: CreateChatCompletionStreamResponseChoices
}

structure CreateChatCompletionStreamResponseChoicesItem {
    index: Integer
    delta: ChatCompletionStreamResponseDelta
    finish_reason: CreateChatCompletionStreamResponseChoicesItemFinishReason
}

structure CreateCompletion200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateCompletionResponse
}

structure CreateCompletionInput {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateCompletionRequest
}

structure CreateCompletionRequest {
    @required
    model: CreateCompletionRequestModel
    @required
    prompt: Prompt
    /// The suffix that comes after a completion of inserted text.
    @dataExamples([
        {
            json: "test."
        }
    ])
    suffix: String
    /// The maximum number of [tokens](/tokenizer) to generate in the completion.
    ///
    /// The token count of your prompt plus `max_tokens` cannot exceed the model's context length. [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_count_tokens_with_tiktoken.ipynb) for counting tokens.
    ///
    @dataExamples([
        {
            json: 16
        }
    ])
    @range(
        min: 0
    )
    max_tokens: Integer
    /// What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.
    ///
    /// We generally recommend altering this or `top_p` but not both.
    ///
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 0
        max: 2
    )
    temperature: Double
    /// An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.
    ///
    /// We generally recommend altering this or `temperature` but not both.
    ///
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 0
        max: 1
    )
    top_p: Double
    /// How many completions to generate for each prompt.
    ///
    /// **Note:** Because this parameter generates many completions, it can quickly consume your token quota. Use carefully and ensure that you have reasonable settings for `max_tokens` and `stop`.
    ///
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 1
        max: 128
    )
    n: Integer
    /// Whether to stream back partial progress. If set, tokens will be sent as data-only [server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#Event_stream_format) as they become available, with the stream terminated by a `data: [DONE]` message. [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_stream_completions.ipynb).
    ///
    stream: Boolean
    /// Include the log probabilities on the `logprobs` most likely tokens, as well the chosen tokens. For example, if `logprobs` is 5, the API will return a list of the 5 most likely tokens. The API will always return the `logprob` of the sampled token, so there may be up to `logprobs+1` elements in the response.
    ///
    /// The maximum value for `logprobs` is 5.
    ///
    @range(
        min: 0
        max: 5
    )
    logprobs: Integer
    /// Echo back the prompt in addition to the completion
    ///
    echo: Boolean
    stop: CreateCompletionRequestStop
    /// Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far, increasing the model's likelihood to talk about new topics.
    ///
    /// [See more information about frequency and presence penalties.](/docs/api-reference/parameter-details)
    ///
    @range(
        min: -2
        max: 2
    )
    presence_penalty: Double
    /// Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far, decreasing the model's likelihood to repeat the same line verbatim.
    ///
    /// [See more information about frequency and presence penalties.](/docs/api-reference/parameter-details)
    ///
    @range(
        min: -2
        max: 2
    )
    frequency_penalty: Double
    /// Generates `best_of` completions server-side and returns the "best" (the one with the highest log probability per token). Results cannot be streamed.
    ///
    /// When used with `n`, `best_of` controls the number of candidate completions and `n` specifies how many to return – `best_of` must be greater than `n`.
    ///
    /// **Note:** Because this parameter generates many completions, it can quickly consume your token quota. Use carefully and ensure that you have reasonable settings for `max_tokens` and `stop`.
    ///
    @range(
        min: 0
        max: 20
    )
    best_of: Integer
    /// Modify the likelihood of specified tokens appearing in the completion.
    ///
    /// Accepts a json object that maps tokens (specified by their token ID in the GPT tokenizer) to an associated bias value from -100 to 100. You can use this [tokenizer tool](/tokenizer?view=bpe) (which works for both GPT-2 and GPT-3) to convert text to token IDs. Mathematically, the bias is added to the logits generated by the model prior to sampling. The exact effect will vary per model, but values between -1 and 1 should decrease or increase likelihood of selection; values like -100 or 100 should result in a ban or exclusive selection of the relevant token.
    ///
    /// As an example, you can pass `{"50256": -100}` to prevent the <|endoftext|> token from being generated.
    ///
    @openapiExtensions(
        "x-oaiTypeLabel": "map"
    )
    logit_bias: Document
    /// A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse. [Learn more](/docs/guides/safety-best-practices/end-user-ids).
    ///
    @dataExamples([
        {
            json: "user-1234"
        }
    ])
    user: String
}

structure CreateCompletionResponse {
    @required
    id: String
    @required
    object: String
    @required
    created: Integer
    @required
    model: String
    @required
    choices: CreateCompletionResponseChoices
    usage: CreateCompletionResponseUsage
}

structure CreateCompletionResponseChoicesItem {
    @required
    text: String
    @required
    index: Integer
    @required
    logprobs: CreateCompletionResponseChoicesItemLogprobs
    @required
    finish_reason: CreateCompletionResponseChoicesItemFinishReason
}

structure CreateCompletionResponseChoicesItemLogprobs {
    tokens: CreateCompletionResponseChoicesItemLogprobsTokens
    token_logprobs: CreateCompletionResponseChoicesItemLogprobsTokenLogprobs
    top_logprobs: CreateCompletionResponseChoicesItemLogprobsTopLogprobs
    text_offset: CreateCompletionResponseChoicesItemLogprobsTextOffset
}

structure CreateCompletionResponseUsage {
    @required
    prompt_tokens: Integer
    @required
    completion_tokens: Integer
    @required
    total_tokens: Integer
}

structure CreateEdit200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateEditResponse
}

structure CreateEditInput {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateEditRequest
}

structure CreateEditRequest {
    @required
    model: CreateEditRequestModel
    /// The input text to use as a starting point for the edit.
    @dataExamples([
        {
            json: "What day of the wek is it?"
        }
    ])
    input: String
    /// The instruction that tells the model how to edit the prompt.
    @dataExamples([
        {
            json: "Fix the spelling mistakes."
        }
    ])
    @required
    instruction: String
    /// How many edits to generate for the input and instruction.
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 1
        max: 20
    )
    n: Integer
    /// What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.
    ///
    /// We generally recommend altering this or `top_p` but not both.
    ///
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 0
        max: 2
    )
    temperature: Double
    /// An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.
    ///
    /// We generally recommend altering this or `temperature` but not both.
    ///
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 0
        max: 1
    )
    top_p: Double
}

structure CreateEditResponse {
    @required
    object: String
    @required
    created: Integer
    @required
    choices: CreateEditResponseChoices
    @required
    usage: CreateEditResponseUsage
}

structure CreateEditResponseChoicesItem {
    text: String
    index: Integer
    logprobs: CreateEditResponseChoicesItemLogprobs
    finish_reason: CreateEditResponseChoicesItemFinishReason
}

structure CreateEditResponseChoicesItemLogprobs {
    tokens: CreateEditResponseChoicesItemLogprobsTokens
    token_logprobs: CreateEditResponseChoicesItemLogprobsTokenLogprobs
    top_logprobs: CreateEditResponseChoicesItemLogprobsTopLogprobs
    text_offset: CreateEditResponseChoicesItemLogprobsTextOffset
}

structure CreateEditResponseUsage {
    @required
    prompt_tokens: Integer
    @required
    completion_tokens: Integer
    @required
    total_tokens: Integer
}

structure CreateEmbedding200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateEmbeddingResponse
}

structure CreateEmbeddingInput {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateEmbeddingRequest
}

structure CreateEmbeddingRequest {
    @required
    model: CreateEmbeddingRequestModel
    @required
    input: CreateEmbeddingRequestInput
    /// A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse. [Learn more](/docs/guides/safety-best-practices/end-user-ids).
    ///
    @dataExamples([
        {
            json: "user-1234"
        }
    ])
    user: String
}

structure CreateEmbeddingResponse {
    @required
    object: String
    @required
    model: String
    @required
    data: CreateEmbeddingResponseData
    @required
    usage: CreateEmbeddingResponseUsage
}

structure CreateEmbeddingResponseDataItem {
    @required
    index: Integer
    @required
    object: String
    @required
    embedding: Embedding
}

structure CreateEmbeddingResponseUsage {
    @required
    prompt_tokens: Integer
    @required
    total_tokens: Integer
}

structure CreateFile200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: OpenAIFile
}

structure CreateFileInput {
    @httpPayload
    @required
    @contentType("multipart/form-data")
    body: CreateFileRequest
}

structure CreateFileRequest {
    /// Name of the [JSON Lines](https://jsonlines.readthedocs.io/en/latest/) file to be uploaded.
    ///
    /// If the `purpose` is set to "fine-tune", each line is a JSON record with "prompt" and "completion" fields representing your [training examples](/docs/guides/fine-tuning/prepare-training-data).
    ///
    @required
    file: Blob
    /// The intended purpose of the uploaded documents.
    ///
    /// Use "fine-tune" for [Fine-tuning](/docs/api-reference/fine-tunes). This allows us to validate the format of the uploaded file.
    ///
    @required
    purpose: String
}

structure CreateFineTune200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: FineTune
}

structure CreateFineTuneInput {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateFineTuneRequest
}

structure CreateFineTuneRequest {
    /// The ID of an uploaded file that contains training data.
    ///
    /// See [upload file](/docs/api-reference/files/upload) for how to upload a file.
    ///
    /// Your dataset must be formatted as a JSONL file, where each training
    /// example is a JSON object with the keys "prompt" and "completion".
    /// Additionally, you must upload your file with the purpose `fine-tune`.
    ///
    /// See the [fine-tuning guide](/docs/guides/fine-tuning/creating-training-data) for more details.
    ///
    @dataExamples([
        {
            json: "file-ajSREls59WBbvgSzJSVWxMCB"
        }
    ])
    @required
    training_file: String
    /// The ID of an uploaded file that contains validation data.
    ///
    /// If you provide this file, the data is used to generate validation
    /// metrics periodically during fine-tuning. These metrics can be viewed in
    /// the [fine-tuning results file](/docs/guides/fine-tuning/analyzing-your-fine-tuned-model).
    /// Your train and validation data should be mutually exclusive.
    ///
    /// Your dataset must be formatted as a JSONL file, where each validation
    /// example is a JSON object with the keys "prompt" and "completion".
    /// Additionally, you must upload your file with the purpose `fine-tune`.
    ///
    /// See the [fine-tuning guide](/docs/guides/fine-tuning/creating-training-data) for more details.
    ///
    @dataExamples([
        {
            json: "file-XjSREls59WBbvgSzJSVWxMCa"
        }
    ])
    validation_file: String
    model: CreateFineTuneRequestModel
    /// The number of epochs to train the model for. An epoch refers to one
    /// full cycle through the training dataset.
    ///
    n_epochs: Integer
    /// The batch size to use for training. The batch size is the number of
    /// training examples used to train a single forward and backward pass.
    ///
    /// By default, the batch size will be dynamically configured to be
    /// ~0.2% of the number of examples in the training set, capped at 256 -
    /// in general, we've found that larger batch sizes tend to work better
    /// for larger datasets.
    ///
    batch_size: Integer
    /// The learning rate multiplier to use for training.
    /// The fine-tuning learning rate is the original learning rate used for
    /// pretraining multiplied by this value.
    ///
    /// By default, the learning rate multiplier is the 0.05, 0.1, or 0.2
    /// depending on final `batch_size` (larger learning rates tend to
    /// perform better with larger batch sizes). We recommend experimenting
    /// with values in the range 0.02 to 0.2 to see what produces the best
    /// results.
    ///
    learning_rate_multiplier: Double
    /// The weight to use for loss on the prompt tokens. This controls how
    /// much the model tries to learn to generate the prompt (as compared
    /// to the completion which always has a weight of 1.0), and can add
    /// a stabilizing effect to training when completions are short.
    ///
    /// If prompts are extremely long (relative to completions), it may make
    /// sense to reduce this weight so as to avoid over-prioritizing
    /// learning the prompt.
    ///
    prompt_loss_weight: Double
    /// If set, we calculate classification-specific metrics such as accuracy
    /// and F-1 score using the validation set at the end of every epoch.
    /// These metrics can be viewed in the [results file](/docs/guides/fine-tuning/analyzing-your-fine-tuned-model).
    ///
    /// In order to compute classification metrics, you must provide a
    /// `validation_file`. Additionally, you must
    /// specify `classification_n_classes` for multiclass classification or
    /// `classification_positive_class` for binary classification.
    ///
    compute_classification_metrics: Boolean
    /// The number of classes in a classification task.
    ///
    /// This parameter is required for multiclass classification.
    ///
    classification_n_classes: Integer
    /// The positive class in binary classification.
    ///
    /// This parameter is needed to generate precision, recall, and F1
    /// metrics when doing binary classification.
    ///
    classification_positive_class: String
    classification_betas: ClassificationBetas
    /// A string of up to 40 characters that will be added to your fine-tuned model name.
    ///
    /// For example, a `suffix` of "custom-model-name" would produce a model name like `ada:ft-your-org:custom-model-name-2022-02-15-04-21-04`.
    ///
    @length(
        min: 1
        max: 40
    )
    suffix: String
}

structure CreateImage200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: ImagesResponse
}

structure CreateImageEdit200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: ImagesResponse
}

structure CreateImageEditInput {
    @httpPayload
    @required
    @contentType("multipart/form-data")
    body: CreateImageEditRequest
}

structure CreateImageEditRequest {
    /// The image to edit. Must be a valid PNG file, less than 4MB, and square. If mask is not provided, image must have transparency, which will be used as the mask.
    @required
    image: Blob
    /// An additional image whose fully transparent areas (e.g. where alpha is zero) indicate where `image` should be edited. Must be a valid PNG file, less than 4MB, and have the same dimensions as `image`.
    mask: Blob
    /// A text description of the desired image(s). The maximum length is 1000 characters.
    @dataExamples([
        {
            json: "A cute baby sea otter wearing a beret"
        }
    ])
    @required
    prompt: String
    /// The number of images to generate. Must be between 1 and 10.
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 1
        max: 10
    )
    n: Integer
    size: CreateImageEditRequestSize
    response_format: CreateImageEditRequestResponseFormat
    /// A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse. [Learn more](/docs/guides/safety-best-practices/end-user-ids).
    ///
    @dataExamples([
        {
            json: "user-1234"
        }
    ])
    user: String
}

structure CreateImageInput {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateImageRequest
}

structure CreateImageRequest {
    /// A text description of the desired image(s). The maximum length is 1000 characters.
    @dataExamples([
        {
            json: "A cute baby sea otter"
        }
    ])
    @required
    prompt: String
    /// The number of images to generate. Must be between 1 and 10.
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 1
        max: 10
    )
    n: Integer
    size: CreateImageRequestSize
    response_format: CreateImageRequestResponseFormat
    /// A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse. [Learn more](/docs/guides/safety-best-practices/end-user-ids).
    ///
    @dataExamples([
        {
            json: "user-1234"
        }
    ])
    user: String
}

structure CreateImageVariation200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: ImagesResponse
}

structure CreateImageVariationInput {
    @httpPayload
    @required
    @contentType("multipart/form-data")
    body: CreateImageVariationRequest
}

structure CreateImageVariationRequest {
    /// The image to use as the basis for the variation(s). Must be a valid PNG file, less than 4MB, and square.
    @required
    image: Blob
    /// The number of images to generate. Must be between 1 and 10.
    @dataExamples([
        {
            json: 1
        }
    ])
    @range(
        min: 1
        max: 10
    )
    n: Integer
    size: CreateImageVariationRequestSize
    response_format: CreateImageVariationRequestResponseFormat
    /// A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse. [Learn more](/docs/guides/safety-best-practices/end-user-ids).
    ///
    @dataExamples([
        {
            json: "user-1234"
        }
    ])
    user: String
}

structure CreateModeration200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateModerationResponse
}

structure CreateModerationInput {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateModerationRequest
}

structure CreateModerationRequest {
    @required
    input: CreateModerationRequestInput
    model: CreateModerationRequestModel
}

structure CreateModerationResponse {
    @required
    id: String
    @required
    model: String
    @required
    results: Results
}

structure CreateModerationResponseResultsItem {
    @required
    flagged: Boolean
    @required
    categories: Categories
    @required
    category_scores: CategoryScores
}

structure CreateTranscription200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateTranscriptionResponse
}

structure CreateTranscriptionInput {
    @httpPayload
    @required
    @contentType("multipart/form-data")
    body: CreateTranscriptionRequest
}

structure CreateTranscriptionRequest {
    /// The audio file object (not file name) to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    ///
    @openapiExtensions(
        "x-oaiTypeLabel": "file"
    )
    @required
    file: Blob
    @required
    model: CreateTranscriptionRequestModel
    /// An optional text to guide the model's style or continue a previous audio segment. The [prompt](/docs/guides/speech-to-text/prompting) should match the audio language.
    ///
    prompt: String
    /// The format of the transcript output, in one of these options: json, text, srt, verbose_json, or vtt.
    ///
    response_format: String
    /// The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will use [log probability](https://en.wikipedia.org/wiki/Log_probability) to automatically increase the temperature until certain thresholds are hit.
    ///
    temperature: Double
    /// The language of the input audio. Supplying the input language in [ISO-639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) format will improve accuracy and latency.
    ///
    language: String
}

structure CreateTranscriptionResponse {
    @required
    text: String
}

structure CreateTranslation200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: CreateTranslationResponse
}

structure CreateTranslationInput {
    @httpPayload
    @required
    @contentType("multipart/form-data")
    body: CreateTranslationRequest
}

structure CreateTranslationRequest {
    /// The audio file object (not file name) translate, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    ///
    @openapiExtensions(
        "x-oaiTypeLabel": "file"
    )
    @required
    file: Blob
    @required
    model: CreateTranslationRequestModel
    /// An optional text to guide the model's style or continue a previous audio segment. The [prompt](/docs/guides/speech-to-text/prompting) should be in English.
    ///
    prompt: String
    /// The format of the transcript output, in one of these options: json, text, srt, verbose_json, or vtt.
    ///
    response_format: String
    /// The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will use [log probability](https://en.wikipedia.org/wiki/Log_probability) to automatically increase the temperature until certain thresholds are hit.
    ///
    temperature: Double
}

structure CreateTranslationResponse {
    @required
    text: String
}

structure DeleteFile200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: DeleteFileResponse
}

structure DeleteFileInput {
    /// The ID of the file to use for this request
    @httpLabel
    @required
    file_id: String
}

structure DeleteFileResponse {
    @required
    id: String
    @required
    object: String
    @required
    deleted: Boolean
}

structure DeleteModel200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: DeleteModelResponse
}

structure DeleteModelInput {
    /// The model to delete
    @dataExamples([
        {
            json: "curie:ft-acmeco-2021-03-03-21-44-20"
        }
    ])
    @httpLabel
    @required
    model: String
}

structure DeleteModelResponse {
    @required
    id: String
    @required
    object: String
    @required
    deleted: Boolean
}

structure DownloadFile200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: String
}

structure DownloadFileInput {
    /// The ID of the file to use for this request
    @httpLabel
    @required
    file_id: String
}

structure Error {
    @required
    type: String
    @required
    message: String
    @required
    param: String
    @required
    code: String
}

structure ErrorResponse {
    @required
    error: Error
}

structure FineTune {
    @required
    id: String
    @required
    object: String
    @required
    created_at: Integer
    @required
    updated_at: Integer
    @required
    model: String
    @required
    fine_tuned_model: String
    @required
    organization_id: String
    @required
    status: String
    @required
    hyperparams: Document
    @required
    training_files: TrainingFiles
    @required
    validation_files: ValidationFiles
    @required
    result_files: ResultFiles
    events: Events
}

structure FineTuneEvent {
    @required
    object: String
    @required
    created_at: Integer
    @required
    level: String
    @required
    message: String
}

structure ImagesResponse {
    @required
    created: Integer
    @required
    data: ImagesResponseData
}

structure ImagesResponseDataItem {
    url: String
    b64_json: String
}

structure ListFiles200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: ListFilesResponse
}

structure ListFilesResponse {
    @required
    object: String
    @required
    data: ListFilesResponseData
}

structure ListFineTuneEvents200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: ListFineTuneEventsResponse
}

structure ListFineTuneEventsInput {
    /// The ID of the fine-tune job to get events for.
    ///
    @dataExamples([
        {
            json: "ft-AF1WoRqd3aJAHsqc9NY7iL8F"
        }
    ])
    @httpLabel
    @required
    fine_tune_id: String
    /// Whether to stream events for the fine-tune job. If set to true,
    /// events will be sent as data-only
    /// [server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#Event_stream_format)
    /// as they become available. The stream will terminate with a
    /// `data: [DONE]` message when the job is finished (succeeded, cancelled,
    /// or failed).
    ///
    /// If set to false, only events generated so far will be returned.
    ///
    @httpQuery("stream")
    stream: Boolean
}

structure ListFineTuneEventsResponse {
    @required
    object: String
    @required
    data: ListFineTuneEventsResponseData
}

structure ListFineTunes200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: ListFineTunesResponse
}

structure ListFineTunesResponse {
    @required
    object: String
    @required
    data: ListFineTunesResponseData
}

structure ListModels200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: ListModelsResponse
}

structure ListModelsResponse {
    @required
    object: String
    @required
    data: ListModelsResponseData
}

structure Model {
    @required
    id: String
    @required
    object: String
    @required
    created: Integer
    @required
    owned_by: String
}

structure OpenAIFile {
    @required
    id: String
    @required
    object: String
    @required
    bytes: Integer
    @required
    created_at: Integer
    @required
    filename: String
    @required
    purpose: String
    status: String
    status_details: Document
}

structure RetrieveFile200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: OpenAIFile
}

structure RetrieveFileInput {
    /// The ID of the file to use for this request
    @httpLabel
    @required
    file_id: String
}

structure RetrieveFineTune200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: FineTune
}

structure RetrieveFineTuneInput {
    /// The ID of the fine-tune job
    ///
    @dataExamples([
        {
            json: "ft-AF1WoRqd3aJAHsqc9NY7iL8F"
        }
    ])
    @httpLabel
    @required
    fine_tune_id: String
}

structure RetrieveModel200 {
    @httpPayload
    @required
    @contentType("application/json")
    body: Model
}

structure RetrieveModelInput {
    /// The ID of the model to use for this request
    @dataExamples([
        {
            json: "text-davinci-001"
        }
    ])
    @httpLabel
    @required
    model: String
}

/// Controls how the model responds to function calls. "none" means the model does not call a function, and responds to the end-user. "auto" means the model can pick between an end-user or calling a function.  Specifying a particular function via `{"name":\ "my_function"}` forces the model to call that function. "none" is the default when no functions are present. "auto" is the default if functions are present.
@untagged
union CreateChatCompletionRequestFunctionCall {
    alt0: CreateChatCompletionRequestFunctionCallOneOfAlt0
    alt1: CreateChatCompletionRequestFunctionCallOneOfAlt1
}

/// Up to 4 sequences where the API will stop generating further tokens.
///
@untagged
union CreateChatCompletionRequestStop {
    String: String
    alt1: CreateChatCompletionRequestStopOneOfAlt1
}

/// Up to 4 sequences where the API will stop generating further tokens. The returned text will not contain the stop sequence.
///
@untagged
union CreateCompletionRequestStop {
    String: String
    alt1: CreateCompletionRequestStopOneOfAlt1
}

/// Input text to embed, encoded as a string or array of tokens. To embed multiple inputs in a single request, pass an array of strings or array of token arrays. Each input must not exceed the max input tokens for the model (8191 tokens for `text-embedding-ada-002`). [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_count_tokens_with_tiktoken.ipynb) for counting tokens.
///
@untagged
union CreateEmbeddingRequestInput {
    String: String
    alt1: CreateEmbeddingRequestInputOneOfAlt1
    alt2: CreateEmbeddingRequestInputOneOfAlt2
    alt3: CreateEmbeddingRequestInputOneOfAlt3
}

/// The input text to classify
@untagged
union CreateModerationRequestInput {
    String: String
    alt1: CreateModerationRequestInputOneOfAlt1
}

/// The prompt(s) to generate completions for, encoded as a string, array of strings, array of tokens, or array of token arrays.
///
/// Note that <|endoftext|> is the document separator that the model sees during training, so if a prompt is not specified the model will generate as if from the beginning of a new document.
///
@untagged
union Prompt {
    String: String
    alt1: CreateCompletionRequestPromptOneOfAlt1
    alt2: CreateCompletionRequestPromptOneOfAlt2
    alt3: CreateCompletionRequestPromptOneOfAlt3
}

/// If this is provided, we calculate F-beta scores at the specified
/// beta values. The F-beta score is a generalization of F-1 score.
/// This is only used for binary classification.
///
/// With a beta of 1 (i.e. the F-1 score), precision and recall are
/// given the same weight. A larger beta score puts more weight on
/// recall and less on precision. A smaller beta score puts more weight
/// on precision and less on recall.
///
@dataExamples([
    {
        json: [
            0.6
            1
            1.5
            2
        ]
    }
])
list ClassificationBetas {
    member: Double
}

@length(
    min: 1
    max: 4
)
list CreateChatCompletionRequestStopOneOfAlt1 {
    member: String
}

list CreateChatCompletionResponseChoices {
    member: CreateChatCompletionResponseChoicesItem
}

list CreateChatCompletionStreamResponseChoices {
    member: CreateChatCompletionStreamResponseChoicesItem
}

list CreateCompletionRequestPromptOneOfAlt1 {
    member: String
}

@dataExamples([
    {
        json: "[1212, 318, 257, 1332, 13]"
    }
])
@length(
    min: 1
)
list CreateCompletionRequestPromptOneOfAlt2 {
    member: Integer
}

@dataExamples([
    {
        json: "[[1212, 318, 257, 1332, 13]]"
    }
])
@length(
    min: 1
)
list CreateCompletionRequestPromptOneOfAlt3 {
    member: CreateCompletionRequestPromptOneOfAlt3Item
}

@length(
    min: 1
)
list CreateCompletionRequestPromptOneOfAlt3Item {
    member: Integer
}

@length(
    min: 1
    max: 4
)
list CreateCompletionRequestStopOneOfAlt1 {
    member: String
}

list CreateCompletionResponseChoices {
    member: CreateCompletionResponseChoicesItem
}

list CreateCompletionResponseChoicesItemLogprobsTextOffset {
    member: Integer
}

list CreateCompletionResponseChoicesItemLogprobsTokenLogprobs {
    member: Double
}

list CreateCompletionResponseChoicesItemLogprobsTokens {
    member: String
}

list CreateCompletionResponseChoicesItemLogprobsTopLogprobs {
    member: Document
}

list CreateEditResponseChoices {
    member: CreateEditResponseChoicesItem
}

list CreateEditResponseChoicesItemLogprobsTextOffset {
    member: Integer
}

list CreateEditResponseChoicesItemLogprobsTokenLogprobs {
    member: Double
}

list CreateEditResponseChoicesItemLogprobsTokens {
    member: String
}

list CreateEditResponseChoicesItemLogprobsTopLogprobs {
    member: Document
}

list CreateEmbeddingRequestInputOneOfAlt1 {
    member: String
}

@dataExamples([
    {
        json: "[1212, 318, 257, 1332, 13]"
    }
])
@length(
    min: 1
)
list CreateEmbeddingRequestInputOneOfAlt2 {
    member: Integer
}

@dataExamples([
    {
        json: "[[1212, 318, 257, 1332, 13]]"
    }
])
@length(
    min: 1
)
list CreateEmbeddingRequestInputOneOfAlt3 {
    member: CreateEmbeddingRequestInputOneOfAlt3Item
}

@length(
    min: 1
)
list CreateEmbeddingRequestInputOneOfAlt3Item {
    member: Integer
}

list CreateEmbeddingResponseData {
    member: CreateEmbeddingResponseDataItem
}

list CreateModerationRequestInputOneOfAlt1 {
    member: String
}

list Embedding {
    member: Double
}

list Events {
    member: FineTuneEvent
}

/// A list of functions the model may generate JSON inputs for.
@length(
    min: 1
)
list Functions {
    member: ChatCompletionFunctions
}

list ImagesResponseData {
    member: ImagesResponseDataItem
}

list ListFilesResponseData {
    member: OpenAIFile
}

list ListFineTuneEventsResponseData {
    member: FineTuneEvent
}

list ListFineTunesResponseData {
    member: FineTune
}

list ListModelsResponseData {
    member: Model
}

/// A list of messages comprising the conversation so far. [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_format_inputs_to_ChatGPT_models.ipynb).
@length(
    min: 1
)
list Messages {
    member: ChatCompletionRequestMessage
}

list ResultFiles {
    member: OpenAIFile
}

list Results {
    member: CreateModerationResponseResultsItem
}

list TrainingFiles {
    member: OpenAIFile
}

list ValidationFiles {
    member: OpenAIFile
}

/// The parameters the functions accepts, described as a JSON Schema object. See the [guide](/docs/guides/gpt/function-calling) for examples, and the [JSON Schema reference](https://json-schema.org/understanding-json-schema/) for documentation about the format.
///
/// To describe a function that accepts no parameters, provide the value `{"type": "object", "properties": {}}`.
document ChatCompletionFunctionParameters

/// The role of the messages author. One of `system`, `user`, `assistant`, or `function`.
enum ChatCompletionRequestMessageRole {
    system
    user
    assistant
    function
}

/// The role of the author of this message.
enum ChatCompletionResponseMessageRole {
    system
    user
    assistant
    function
}

/// The role of the author of this message.
enum ChatCompletionStreamResponseDeltaRole {
    system
    user
    assistant
    function
}

enum CreateChatCompletionRequestFunctionCallOneOfAlt0 {
    none
    auto
}

enum CreateChatCompletionResponseChoicesItemFinishReason {
    stop
    length
    function_call
}

enum CreateChatCompletionStreamResponseChoicesItemFinishReason {
    stop
    length
    function_call
}

enum CreateCompletionResponseChoicesItemFinishReason {
    stop
    length
}

enum CreateEditResponseChoicesItemFinishReason {
    stop
    length
}

/// The format in which the generated images are returned. Must be one of `url` or `b64_json`.
enum CreateImageEditRequestResponseFormat {
    url
    b64_json
}

/// The size of the generated images. Must be one of `256x256`, `512x512`, or `1024x1024`.
enum CreateImageEditRequestSize {
    n256x256 = "256x256"
    n512x512 = "512x512"
    n1024x1024 = "1024x1024"
}

/// The format in which the generated images are returned. Must be one of `url` or `b64_json`.
enum CreateImageRequestResponseFormat {
    url
    b64_json
}

/// The size of the generated images. Must be one of `256x256`, `512x512`, or `1024x1024`.
enum CreateImageRequestSize {
    n256x256 = "256x256"
    n512x512 = "512x512"
    n1024x1024 = "1024x1024"
}

/// The format in which the generated images are returned. Must be one of `url` or `b64_json`.
enum CreateImageVariationRequestResponseFormat {
    url
    b64_json
}

/// The size of the generated images. Must be one of `256x256`, `512x512`, or `1024x1024`.
enum CreateImageVariationRequestSize {
    n256x256 = "256x256"
    n512x512 = "512x512"
    n1024x1024 = "1024x1024"
}
