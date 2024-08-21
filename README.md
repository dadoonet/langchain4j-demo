# LangChain4j with Elasticsearch demos

## Introduction

This is a repository which contains examples on how to use LangChain4j with Elasticsearch.

## Prerequisites

None yet

## Demos

### Demo 1: the basics

The first demo is only making a simple connection to OpenAI to ask it 
a simple question.

See [Step1AiChatTest.java](src/test/java/fr/pilato/demo/Step1AiChatTest.java) class.

### Demo 2: with more context

We can give some context before asking questions to the LLM.

See [Step2AssistantTest.java](src/test/java/fr/pilato/demo/Step2AssistantTest.java) class.

### Demo 3: running the LLM locally with Ollama

We can use the great [Ollama project](https://ollama.com/). It helps to run a LLM locally
on your machine.

See [Step3OllamaTest.java](src/test/java/fr/pilato/demo/Step3OllamaTest.java) class.

### Demo 4: add Memory

When asking consecutive questions, we need to remember the context of the 
discussion.

See [Step4WithMemoryTest.java](src/test/java/fr/pilato/demo/Step4WithMemoryTest.java) class.
