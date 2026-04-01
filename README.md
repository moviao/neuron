<div align="center">

# ⚡ Neuron — Local LLM Chat UI

**A beautiful, privacy-first chat interface for your local AI server**

[![Quarkus](https://img.shields.io/badge/Quarkus-3.15.3-4695EB?style=flat-square&logo=quarkus&logoColor=white)](https://quarkus.io)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Gradle](https://img.shields.io/badge/Gradle-8.10-02303A?style=flat-square&logo=gradle&logoColor=white)](https://gradle.org)
[![Docker Hub](https://img.shields.io/docker/pulls/moviao/neuron?style=flat-square&logo=docker&logoColor=white&label=Docker%20Hub)](https://hub.docker.com/r/moviao/neuron)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)
[![CI](https://img.shields.io/github/actions/workflow/status/YOUR_USERNAME/llm-chat-ui/ci.yml?branch=main&style=flat-square&label=CI)](https://github.com/YOUR_USERNAME/llm-chat-ui/actions)

*No cloud. No tracking. No API keys. Just your model, your machine, your data.*

</div>

---

## ✨ Overview

**Neuron** is a sleek, self-hosted web chat interface that connects to any **OpenAI-compatible local LLM REST API** — such as [Ollama](https://ollama.ai), [llama.cpp server](https://github.com/ggerganov/llama.cpp), [LM Studio](https://lmstudio.ai), or [Jan](https://jan.ai).

The backend is a lightweight **Quarkus** service written in **Kotlin** that proxies requests between your browser and your local model, serving a beautiful dark-themed single-page UI.

```
Browser → http://localhost:8082  (Neuron UI)
                  ↕  /api/chat, /api/models
        Quarkus Backend  (port 8082)
                  ↕  OpenAI-compatible REST
        Local LLM Server (port 8081)
```

---

![Neuron Chat UI](https://raw.githubusercontent.com/moviao/neuron/refs/heads/main/docs/screenshot.png)




## 🎯 Features

- **Zero telemetry** — everything stays on your machine
- **OpenAI-compatible** — works with any server implementing `/v1/chat/completions`
- **Multi-conversation** sidebar with persistent localStorage history
- **Live model selector** — auto-fetched from your LLM server's `/v1/models`
- **Temperature slider** — adjust creativity per message
- **System prompt** — collapsible, per-session
- **Markdown rendering** — code blocks, bold, italic
- **Token usage badges** — prompt / completion / total
- **Export** conversations to `.txt`
- **Responsive** — works on desktop and mobile
- **Docker-ready** — one command to containerize

---

## 🚀 Quick Start

### Option 1 — Docker Hub (recommended, no build needed)

The fastest way to run Neuron is to pull the precompiled image directly from Docker Hub.

**Prerequisites:** Docker · your local LLM server running (e.g. llama.cpp on port 8081)

```bash
# Pull & run — point LLM_API_URL at your local server
docker run -d \
  --name neuron \
  -p 8082:8082 \
  -e LLM_API_URL=http://host.docker.internal:8081 \
  moviao/neuron:latest
```

Then open **http://localhost:8082** — that's it. 🎉

> `host.docker.internal` lets the container reach a server running on your host machine.
> On Linux, add `--add-host=host.docker.internal:host-gateway` if it doesn't resolve.

```bash
# Linux variant
docker run -d \
  --name neuron \
  -p 8082:8082 \
  --add-host=host.docker.internal:host-gateway \
  -e LLM_API_URL=http://host.docker.internal:8081 \
  moviao/neuron:latest
```

**Useful run-time overrides:**

```bash
docker run -d \
  --name neuron \
  -p 8082:8082 \
  --add-host=host.docker.internal:host-gateway \
  -e LLM_API_URL=http://host.docker.internal:8081 \
  -e LLM_API_MODEL=my-model \
  -e LLM_API_MAX_TOKENS=4096 \
  -e LLM_API_TEMPERATURE=0.8 \
  moviao/neuron:latest
```

**Or with Docker Compose:**

```yaml
# docker-compose.yml
services:
  neuron:
    image: moviao/neuron:latest
    ports:
      - "8082:8082"
    environment:
      LLM_API_URL: http://host.docker.internal:8081
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

```bash
docker compose up -d
```

---

### Option 2 — Build from source

**Prerequisites:** Java 21+ · Gradle wrapper included (no install needed) · local LLM server

```bash
# 1. Clone
git clone https://github.com/YOUR_USERNAME/llm-chat-ui.git
cd llm-chat-ui

# 2. Build
./gradlew build

# 3. Run
java -jar build/quarkus-app/quarkus-run.jar
```

Open **http://localhost:8082**.

---

## ⚙️ Configuration

All settings live in `src/main/resources/application.properties`:

```properties
# Port for the Neuron UI
quarkus.http.port=8082

# Your local LLM server
llm.api.url=http://localhost:8080
llm.api.model=local-model
llm.api.max-tokens=2048
llm.api.temperature=0.7

# REST client timeouts (ms)
quarkus.rest-client."com.llmchat.service.LlmApiClient".connect-timeout=10000
quarkus.rest-client."com.llmchat.service.LlmApiClient".read-timeout=120000
```

Override any property at runtime without recompiling:

```bash
java -jar build/quarkus-app/quarkus-run.jar \
  -Dllm.api.url=http://localhost:11434 \
  -Dllm.api.model=llama3.2
```

Or via environment variables (Quarkus converts them automatically):

```bash
export LLM_API_URL=http://localhost:11434
export LLM_API_MODEL=llama3.2
java -jar build/quarkus-app/quarkus-run.jar
```

---

## 🐳 Docker

### Pull from Docker Hub

```bash
docker pull moviao/neuron:latest
```

### Build your own image

```bash
./gradlew build
docker build -f src/main/docker/Dockerfile.jvm -t moviao/neuron .
```

### Push a new version to Docker Hub

```bash
./gradlew build
docker build -f src/main/docker/Dockerfile.jvm -t moviao/neuron:latest .
docker push moviao/neuron:latest
```

---

## 🏗️ Project Structure

```
llm-chat-ui/
├── build.gradle.kts                    # Gradle build (Kotlin DSL)
├── settings.gradle.kts
├── gradle.properties                   # Version pins
├── gradle/wrapper/                     # Gradle wrapper (no install needed)
├── gradlew / gradlew.bat
├── docker-compose.yml
├── .github/
│   └── workflows/
│       └── ci.yml                      # GitHub Actions CI pipeline
└── src/
    └── main/
        ├── docker/
        │   └── Dockerfile.jvm
        ├── kotlin/com/llmchat/
        │   ├── model/
        │   │   └── Models.kt           # Request / response data classes
        │   ├── service/
        │   │   ├── LlmApiClient.kt     # MicroProfile REST Client → LLM
        │   │   └── ChatService.kt      # Business logic & request proxy
        │   └── resource/
        │       └── ChatResource.kt     # REST endpoints (/api/*)
        └── resources/
            ├── application.properties
            └── META-INF/resources/
                └── index.html          # Single-page chat UI
```

---

## 📡 REST API Reference

### `POST /api/chat`

Sends a chat message. The server proxies it to your LLM server.

```bash
curl -X POST http://localhost:8082/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      { "role": "system", "content": "You are a helpful assistant." },
      { "role": "user",   "content": "Explain Quarkus in one sentence." }
    ],
    "model": "local-model",
    "temperature": 0.7
  }'
```

**Response:**
```json
{
  "message": { "role": "assistant", "content": "Quarkus is a Kubernetes-native Java framework..." },
  "model": "local-model",
  "usage": { "prompt_tokens": 28, "completion_tokens": 42, "total_tokens": 70 }
}
```

### `GET /api/models`

Returns the model list from your LLM server.

```bash
curl http://localhost:8082/api/models
```

### `GET /api/health`

Health check.

```bash
curl http://localhost:8082/api/health
# → {"status":"ok","service":"llm-chat-ui"}
```

---

## 🛠️ Development

```bash
# Start with live reload
./gradlew quarkusDev

# Run tests
./gradlew test

# Build production JAR
./gradlew build

# Clean
./gradlew clean
```

When running in dev mode, Quarkus provides its **Dev UI** at `http://localhost:8082/q/dev/`.

---

## 🔧 Compatible LLM Servers

| Server                                                     | Default port | Configuration                        |
|------------------------------------------------------------|--------------|--------------------------------------|
| [Kortex](https://github.com/moviao/kortex)                 | `8081`       | `llm.api.url=http://localhost:8081`  |
| [Ollama](https://ollama.ai)                                | `11434`      | `llm.api.url=http://localhost:11434` |
| [llama.cpp server](https://github.com/ggerganov/llama.cpp) | `8080`       | `llm.api.url=http://localhost:8080`  |
| [LM Studio](https://lmstudio.ai)                           | `1234`       | `llm.api.url=http://localhost:1234`  |
| [Jan](https://jan.ai)                                      | `1337`       | `llm.api.url=http://localhost:1337`  |
| [vLLM](https://github.com/vllm-project/vllm)               | `8000`       | `llm.api.url=http://localhost:8000`  |
| Any OpenAI-compatible                                      | custom       | Point `llm.api.url` at it            |

---

## 🚢 Deploying to GitHub

```bash
# Initialize the repo locally
git init
git add .
git commit -m "feat: initial Neuron LLM chat UI"

# Push to GitHub (create the repo on github.com first)
git remote add origin https://github.com/YOUR_USERNAME/llm-chat-ui.git
git branch -M main
git push -u origin main
```

The included GitHub Actions workflow (`.github/workflows/ci.yml`) will automatically build and test on every push to `main` or `develop`, and build the Docker image on merges to `main`.

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Commit your changes: `git commit -m 'feat: add my feature'`
4. Push: `git push origin feat/my-feature`
5. Open a Pull Request

Please follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---








# Hi, I'm H 👋

Principal Engineer & Founder of **HD International Ltd** — a Bulgarian-based software consultancy delivering
enterprise-grade solutions across Architecture, Cloud, AI, DevOps, and Operations.

---

## 🏢 HD International Ltd

> *Engineering clarity from complexity.*

We partner with startups and enterprises to design, build, and operate resilient, scalable systems.

---

## 🛠️ Services

**Software Architecture**
Designing scalable, maintainable systems — microservices, event-driven architecture, domain-driven
design, and API strategy.

**Cloud Engineering** · AWS · GCP · Azure . On Premise
Infrastructure design, cloud migrations, cost optimisation, and multi-cloud strategies.

**DevOps & CI/CD**
End-to-end pipeline automation, containerisation (Docker/Kubernetes), GitOps, and platform engineering.

**AI & ML Solutions**
Integrating LLMs, building ML pipelines, and delivering production-ready AI features into existing products.

**Operations & SRE**
Observability, incident management, SLA design, and reliability engineering for critical systems.

---

## 🧰 Core Stack

**Languages**
`Java` `C#` `PHP 8+` `Python` `Go` `TypeScript` `JavaScript` `Rust` `Bash` `Ruby` `Scala` `Kotlin`

**Frameworks & Libraries**
`Spring Boot` `ASP.NET Core` `Laravel` `Symfony` `FastAPI` `Django` `Flask` `NestJS` `Express`
`React` `Next.js` `Angular` `LangChain` `LlamaIndex` `Celery` `GraphQL`

**Cloud & Infrastructure**
`AWS` `GCP` `Azure` `Terraform` `Pulumi` `CDK` `Ansible` `Packer` `Vault` `Consul`

**Containers & Orchestration**
`Docker` `Kubernetes` `Helm` `Istio` `ArgoCD` `FluxCD` `Kustomize` `Rancher` `OpenShift`

**CI/CD & DevOps**
`GitHub Actions` `GitLab CI` `Jenkins` `CircleCI` `TeamCity` `Tekton` `SonarQube` `Nexus`

**Data & Messaging**
`PostgreSQL` `MySQL` `MongoDB` `Redis` `Elasticsearch` `Kafka` `RabbitMQ` `NATS` `Kinesis`
`Snowflake` `BigQuery` `dbt` `Airflow` `Spark`

**AI & ML**
`LangChain` `LlamaIndex` `OpenAI API` `Anthropic API` `HuggingFace` `PyTorch` `TensorFlow`
`scikit-learn` `MLflow` `Weights & Biases` `Pinecone` `Weaviate` `FAISS`

**Observability & Operations**
`Datadog` `Prometheus` `Grafana` `Loki` `Jaeger` `OpenTelemetry` `PagerDuty` `Sentry`
`ELK Stack` `New Relic` `Dynatrace`

**Security & Compliance**
`Vault` `Trivy` `Snyk` `OWASP ZAP` `Falco` `Checkov` `AWS Security Hub` `IAM` `OAuth2` `OIDC`

---

## 📬 Work With Us

We take on select consulting engagements and fractional CTO mandates.

📧 hdinternational82@gmail.com 
💼 [LinkedIn]([https://www.linkedin.com/in/time-to-move-h/])

> *Registered in Bulgaria & Plovdiv · HD International Ltd*
> VAT BG-204723748


<div align="center">

Built with ⚡ using [Quarkus](https://quarkus.io) · [Kotlin](https://kotlinlang.org) · [Gradle](https://gradle.org)

*Your AI. Your machine. Your rules.*

</div>
