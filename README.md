# CortexGate Orchestrator Service

CortexGate is a Spring Boot orchestration service designed to manage intelligent RAG (Retrieval-Augmented Generation) workflows. It utilizes Spring AI to interface with Large Language Models (LLMs) and Vector Databases.

## Features

- **Spring AI Integration**: Uses Gemini 2.5 Flash for chat generation and text-embedding-004 for vector embeddings.
- **MongoDB Atlas Local**: Stores and queries high-dimensional vector data directly using MongoDB's vector search capabilities.
- **Kafka Event Streaming**: Connects to an event bus for processing asynchronous orchestration requests.
- **PostgreSQL**: Stores relational metadata and long-term state.

## Prerequisites

- Docker and Docker Compose
- Java 21+ (Java 25 was used in initial environment)
- Maven 3.8+

## Running the Infrastructure

To run the backing infrastructure (MongoDB, PostgreSQL, Kafka, and Zookeeper), you can use Docker Compose:

```bash
docker-compose up -d
```

### Services Started:
- **MongoDB Atlas Local**: `localhost:27017`
- **PostgreSQL**: `localhost:5434` (User: `cortexuser`, Password: `cortexpassword`)
- **Kafka Broker**: `localhost:9092`
- **Zookeeper**: `localhost:2181`

## Building and Running the Application

You can build the `orchestrator-service` via Maven:

```bash
cd orchestrator-service
mvn clean compile
```

To run the application locally:

```bash
mvn spring-boot:run
```

The application will bind to port `8081` by default.

## Configuration

Make sure your `application.yml` has the correct `api-key` for OpenAI/Google Gemini models:

```yaml
spring:
  ai:
    openai:
      api-key: <YOUR_API_KEY>
```
