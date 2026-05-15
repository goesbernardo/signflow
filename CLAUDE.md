# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start local dependencies (PostgreSQL + Kafka)
docker-compose up -d

# Build
mvn clean package -DskipTests

# Run (local profile)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=AuthIntegrationTest

# Run a single test method
mvn test -Dtest=AuthIntegrationTest#methodName
```

Swagger UI is available at `http://localhost:8080/swagger-ui.html` when running locally.

## Architecture

SignFlow is a **multi-provider e-signature gateway** built with Spring Boot 3.3.4 / Java 17. It follows **Hexagonal Architecture** (Ports & Adapters):

```
com.signflow/
├── api/                       # REST controllers (inbound adapters)
├── application/
│   ├── port/in/               # Inbound port interfaces (service contracts)
│   ├── port/out/              # Outbound port interfaces (gateway contracts)
│   ├── service/               # Business logic implementations
│   └── webhook/               # Webhook processing pipeline
├── domain/
│   ├── command/               # Provider-agnostic command objects
│   ├── exception/             # Domain exceptions with error codes
│   └── model/                 # Domain entities
├── enums/                     # Neutral enums shared across layers
└── infrastructure/
    ├── gateway/               # SignatureGatewayRegistry (routes to providers)
    ├── persistence/           # JPA entities, repositories, Flyway migrations
    ├── provider/clicksign/    # ClickSign adapter (client, mapper, webhook handler)
    └── security/              # AES-256/GCM encryption utilities
```

### Key Patterns

**Provider Abstraction**: All e-signature providers (ClickSign, D4Sign, ZapSign, DocuSign) implement `ESignatureGateway` (outbound port). `SignatureGatewayRegistry` selects the correct provider at runtime based on tenant configuration. Adding a new provider means creating a new adapter under `infrastructure/provider/<name>/` and registering it.

**Smart Routing**: `SmartRoutingService` in `application/service/` selects providers based on tenant config, document type, and fallback rules using Resilience4j circuit breakers.

**Domain Commands**: Controllers translate HTTP requests into provider-agnostic commands (`domain/command/`) before passing them to services, keeping the domain layer free of HTTP concerns.

**Webhook Pipeline**: Incoming webhooks flow through `application/webhook/` where they are validated (HMAC), normalized to domain events, and published to Kafka topics for downstream processing.

**Tenant Isolation**: Multi-tenant via `TenantContext` (thread-local) populated by the security filter chain. Repositories automatically scope queries to the current tenant.

### Infrastructure

| Concern | Technology |
|---|---|
| Database | PostgreSQL 14+ with Flyway migrations (`resources/db/migration/`) |
| Messaging | Apache Kafka (Upstash in prod) |
| HTTP Client | OpenFeign with Resilience4j circuit breaker + rate limiter |
| Security | Spring Security + JWT HS512 + AES-256/GCM |
| i18n | Messages in `resources/messages*.properties` (pt-BR, en, es, it) |

### Profiles

- `local` — uses Docker Compose services, has fallback defaults for secrets
- `prod` — all secrets via environment variables, no fallbacks

Required env vars for `prod`: `SPRING_DATASOURCE_URL`, `JWT_SECRET` (≥64 chars), `SIGNFLOW_ENCRYPTION_KEY` (≥32 bytes), `CLICKSIGN_URL`, `CLICKSIGN_API_TOKEN`, `CLICKSIGN_WEBHOOK_SECRET`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SPRING_MAIL_*`, `SIGNFLOW_ADMIN_USERNAME`, `SIGNFLOW_ADMIN_PASSWORD`.
