# CortexGate Project Documentation

## 1. What is CortexGate?
CortexGate is an intelligent, scalable orchestration service and API gateway designed to manage Retrieval-Augmented Generation (RAG) workflows. At its core, the project provides a unified backend architecture for AI-driven chat applications. It ingests, embeds, and indexes document knowledge, subsequently utilizing that context to answer user queries accurately through Large Language Models (LLMs). The architecture is event-driven, decoupled, and designed for high throughput.

## 2. What We Used (Technology Stack)

### Backend & Core Services
- **Java 21 & Spring Boot**: The foundational framework powering the API Gateway and the Orchestrator Service.
- **Spring AI**: The AI abstraction layer used to seamlessly interface with AI components.
- **Google Gemini (2.5 Flash & Text-Embedding-004)**: The underlying LLM for high-speed conversational reasoning, and the embedding model for converting text into vector arrays.

### Infrastructure & Databases
- **MongoDB Atlas Local**: The primary Vector Store for semantic similarity searches.
- **PostgreSQL**: A robust relational database for persistent application state, such as saving `InteractionRecord`s.
- **Apache Kafka & Zookeeper**: The message broker ecosystem handling asynchronous, event-driven communication between the Gateway and the Orchestrator.
- **Docker & Docker Compose**: Containerization technologies used to define and run the complex multi-database infrastructure locally.

### Frontend
- **React & Vite**: A fast, modern JavaScript frontend framework providing the interactive user interface (Chat UI).

---

## 3. Why We Used It (Architecture Rationale)

- **Spring AI & Java**: Spring Boot is industry-standard for scalable enterprise backends. Spring AI allowed us to cleanly abstract the complexity of interacting with the Google Gemini APIs without writing brittle HTTP wrappers.
- **MongoDB Atlas Local**: Traditional databases cannot efficiently query for "similarity". MongoDB provides a powerful, hybrid NoSQL and Vector Database solution. Running the Atlas version locally means we get high-end vector similarity searches (Cosine Similarity) on our document embeddings without incurring cloud costs during development.
- **PostgreSQL**: While Mongo handles vector data, Postgres is unmatched for strict, ACID-compliant relational data. It is used to safely log chat histories, user interactions, and metadata.
- **Apache Kafka**: AI generations can be slow and unpredictable. Instead of the API Gateway blocking HTTP threads while waiting for Gemini to answer, the Gateway publishes a `ChatRequest` to a Kafka topic. The Orchestrator consumes this, processes it, and publishes the result to a response topic. This decoupled design ensures the application remains responsive under heavy load.
- **Docker Compose**: Running Postgres, Mongo, Kafka, and Zookeeper natively on a developer machine is prone to port conflicts and version mismatches. Docker Compose ensures the entire infrastructure can be spun up in a single command (`docker-compose up -d`) in a reproducible, isolated environment.

---

## 4. How We Used It (System Flow)

1. **User Interaction**: The user submits a prompt via the React frontend.
2. **Gateway to Broker**: The request hits the Spring Boot `api-gateway`, which upgrades to a WebSocket connection. The gateway wraps the message and drops it into a Kafka topic (`chat-requests`).
3. **Orchestrator Ingestion**: The `orchestrator-service` constantly listens to this Kafka topic. Upon receiving the message, it asks the Gemini `text-embedding-004` model to convert the user's prompt into a mathematical vector.
4. **Context Retrieval (RAG)**: The Orchestrator queries **MongoDB** using this vector to find the most semantically similar documents (context) in its database.
5. **LLM Generation**: The Orchestrator bundles the original prompt and the retrieved context, sending it to **Gemini 2.5 Flash** for a final, grounded answer.
6. **Persistence**: The system logs this exchange into **PostgreSQL** (via JPA/Hibernate) to maintain a persistent interaction record.
7. **Response Delivery**: The generated answer is published back to a Kafka response topic. The `api-gateway` consumes this and streams it down the WebSocket connection to the React frontend, displaying the answer to the user.
