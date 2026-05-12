# SignFlow

> **Gateway de Assinatura Eletrônica Multi-Provider**  
> Uma API unificada que abstrai múltiplos provedores de assinatura eletrônica — integre uma vez, use qualquer provider.

---

## Visão Geral

O SignFlow é uma plataforma backend construída com **Spring Boot** que funciona como camada de abstração entre sua aplicação e os provedores de assinatura eletrônica do mercado.

Em vez de integrar cada provedor de forma isolada — lidando com APIs diferentes, autenticações diferentes e webhooks em formatos diferentes — você integra o SignFlow uma única vez e ganha acesso a todos eles com uma API padronizada, webhook normalizado e smart routing automático.

### Por que o SignFlow?

| Problema atual | O que o SignFlow resolve |
|---|---|
| Lock-in com um único provider | Troque de provider sem alterar uma linha de código |
| Múltiplas integrações para múltiplos providers | Uma API, uma autenticação, um formato de webhook |
| Sem controle de custo por provider | Smart routing automático por regras configuráveis |
| Provider caiu, contratos parados | Fallback automático para provider secundário |
| Polling para saber se o contrato foi assinado | Webhook de saída em tempo real com retry automático |

---



## Arquitetura

O projeto segue os princípios da **Arquitetura Hexagonal** (Ports and Adapters):

```
signflow/
├── api/                          # Controllers e DTOs (entrada)
├── application/
│   ├── port/
│   │   ├── in/                   # Interfaces de entrada (SignatureService)
│   │   └── out/                  # Interfaces de saída (ESignatureGateway)
│   ├── service/                  # Implementações de negócio
│   └── webhook/                  # Pipeline de eventos (Kafka consumer → processor → outbound)
├── domain/
│   ├── command/                  # Objetos de comando — neutros de provider
│   ├── exception/                # Exceções com códigos semânticos (DomainErrorCode)
│   └── model/                   # Modelos de domínio
├── enums/                        # Enums neutros (SignatureAuthMethod, SignerRole...)
└── infrastructure/
    ├── exception/                # Tratamento global de erros (GlobalExceptionHandler)
    ├── gateway/                  # Registry de providers (SignatureGatewayRegistry)
    ├── persistence/              # Entities, Repositories, Migrations (Flyway)
    ├── security/                 # EncryptionConverter (AES/GCM)
    └── provider/
        └── clicksign/            # Gateway ClickSign — client, mapper, webhook handler
```

### Pipeline de Webhook

```
Provider (ClickSign)
    ↓ POST /webhook/{provider}
WebhookController → publica em Kafka (202 Accepted imediato)
    ↓ signflow.webhook.received
WebhookConsumer (@KafkaListener) → chama WebhookHandler
    ↓
WebhookEventProcessor → persiste evento + atualiza banco
    ↓
OutboundWebhookService (@Async) → notifica callbackUrl do cliente
    ↓ falha → PENDING_RETRY → @Scheduled retry (60s → 600s)
    ↓ 3 falhas → Dead Letter Queue (signflow.webhook.outbound.dlq)
```

### Princípio central de domínio

Os `Commands` usam enums neutros — `SignatureAuthMethod`, `SignerRole`, `NotificationChannel`. Cada gateway mapeia esses enums para os tipos específicos do seu provider sem contaminar o domínio. Adicionar um novo provider não altera nenhum endpoint ou regra de negócio existente.

---

## Design Patterns & SOLID

O projeto foi construído focando em manutenibilidade e extensibilidade, utilizando padrões consagrados:

### SOLID
- **S - Single Responsibility Principle**: Cada serviço (ex: `AuditLogService`, `OutboundWebhookService`) possui uma única responsabilidade clara.
- **O - Open/Closed Principle**: Novos provedores de assinatura podem ser adicionados sem alterar o código core do sistema, apenas implementando a interface `ESignatureGateway`.
- **L - Liskov Substitution Principle**: Todas as implementações de `ESignatureGateway` (ex: `ClickSignGateway`) podem ser substituídas sem quebrar o `SignatureServiceImpl`.
- **I - Interface Segregation Principle**: Interfaces granulares para entrada (`SignatureService`) e saída (`ESignatureGateway`).
- **D - Dependency Inversion Principle**: O core do negócio depende de abstrações (`Ports`), não de implementações concretas (`Adapters`).

### Design Patterns
- **Strategy**: Utilizado no roteamento inteligente (`SmartRoutingService`) e na seleção de gateways para diferentes provedores.
- **Adapter**: A infraestrutura de cada provider adapta a API externa para o formato neutro do SignFlow.
- **Factory / Registry**: `SignatureGatewayRegistry` centraliza e fornece a instância correta do gateway baseado no provider selecionado.
- **Observer (Event-Driven)**: Utilização de Kafka para desacoplar o recebimento de webhooks do seu processamento e da notificação de saída.
- **Template Method**: Padronização do fluxo de processamento de eventos de webhook.
- **DTO (Data Transfer Object)**: Uso extensivo para trafegar dados entre camadas sem expor entidades JPA.

---

## Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Runtime | Java 17 |
| Framework | Spring Boot 3.x |
| Segurança | Spring Security + JWT HS512 + AES/GCM |
| Mensageria | Apache Kafka (Upstash) |
| Banco de dados | PostgreSQL |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| HTTP Client | OpenFeign |
| Resiliência | Resilience4j (Circuit Breaker + Rate Limiter) |
| Documentação | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |


---

## Providers Suportados

| Provider | Status | Autenticações |
|---|---|---|
| ClickSign | ✅ Implementado e testado | EMAIL, SMS, WhatsApp, Pix, Manuscrita, Biometria Facial, API |
| D4Sign | 🔜 Em desenvolvimento | — |
| ZapSign | 📋 Mapeado | — |
| DocuSign | 📋 Mapeado | — |

---

## Endpoints

A documentação interativa completa está disponível via **Swagger UI**:
- Local: `http://localhost:8080/swagger-ui.html`
- Produção: `https://signflow.api.br/swagger-ui.html`

---

## Configuração

### Pré-requisitos

- Java 17+
- PostgreSQL 14+
- Maven 3.8+
- Kafka (local ou Upstash)

### Variáveis de ambiente

Crie um arquivo `.env` baseado no exemplo abaixo. **Nunca commite credenciais reais.**

```env
# Banco de dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/signflow
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha

# JWT — mínimo 64 caracteres (512 bits para HS512)
# gere com: openssl rand -hex 64
JWT_SECRET=

# Criptografia de dados sensíveis — mínimo 32 bytes (AES-256)
# gere com: openssl rand -base64 32
SIGNFLOW_ENCRYPTION_KEY=

# ClickSign
CLICKSIGN_URL=https://sandbox.clicksign.com/api/v3
CLICKSIGN_API_TOKEN=
CLICKSIGN_WEBHOOK_SECRET=

# Kafka (Upstash em produção)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_API_KEY=
KAFKA_API_SECRET=

# Swagger
SWAGGER_SERVER_URL=https://signflow.api.br
```

### Executando localmente

```bash
# Clonar o repositório
git clone https://github.com/goesbernardo/signflow.git
cd signflow

# Subir PostgreSQL e Kafka com Docker
docker-compose up -d

# Executar a aplicação
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

A aplicação estará disponível em `http://localhost:8080`.

---

### Executando em Produção na AWS (Docker)

Para rodar em um ambiente de produção na AWS (EC2 ou ECS):

```bash
# Inicie os serviços em modo background (certifique-se que as vars de ambiente estão setadas)
docker-compose -f docker-compose.prod.yaml up -d
```

O `docker-compose.prod.yaml` inclui:
- **Aplicação**: Imagem buildada localmente (ou via CI/CD AWS), rodando com perfil `prod` e healthcheck configurado.
- **Banco de Dados**: PostgreSQL 16 (Alpine) com persistência em volume EBS/RDS e healthcheck.
- **Rede Isolada**: O banco de dados não expõe portas para o host, apenas para a aplicação.

> Nota: O Kafka é esperado via variáveis de ambiente (ex: Upstash ou AWS MSK) para maior resiliência em produção.

---

## Fluxo de Assinatura

### Criar envelope completo em uma chamada

```bash
curl -X POST http://localhost:8080/v1/signatures/create-activate-envelope \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -H "provider: CLICKSIGN" \
  -d '{
    "name": "Contrato de Prestação de Serviços",
    "documents": [{
      "filename": "contrato.pdf",
      "content_base64": "data:application/pdf;base64,..."
    }],
    "signers": [{
      "name": "João Silva",
      "email": "joao@empresa.com",
      "hasDocumentation": false,
      "notificationChannel": "EMAIL"
    }],
    "requirements": [{
      "role": "SIGN",
      "auth": "EMAIL"
    }],
    "autoActivate": true,
    "callbackUrl": "https://meu-sistema.com/webhook/signflow"
  }'
```

> O header `provider` é opcional quando há regras de Smart Routing configuradas.


## Smart Routing

Configure regras para que o SignFlow selecione o provider automaticamente:

```bash
# Sempre usar ClickSign
POST /v1/routing-rules
{ "priority": 1, "conditionType": "ALWAYS", "provider": "CLICKSIGN", "active": true }

# Usar D4Sign quando auth for PIX
POST /v1/routing-rules
{ "priority": 2, "conditionType": "AUTH_METHOD", "conditionValue": "PIX", "provider": "D4SIGN", "active": true }
```

Quando o header `provider` não for informado, o SignFlow avalia as regras por prioridade e roteia automaticamente.

---

## Webhook de Saída

Informe `callbackUrl` ao criar o envelope para receber eventos em tempo real:

```json
{
  "envelopeId": "uuid-do-envelope",
  "provider": "CLICKSIGN",
  "eventType": "DOCUMENT_COMPLETED",
  "status": "CLOSED",
  "occurredAt": "2026-05-10T14:30:00Z"
}
```

O sistema tenta a entrega até 3 vezes com backoff progressivo (0s → 60s → 600s). Falhas persistentes vão para a Dead Letter Queue. O histórico está disponível em `GET /signatures/{id}/webhook-deliveries`.

---

## Segurança

- **JWT HS512** — tokens sem fallback de secret; aplicação não sobe sem `JWT_SECRET` configurado
- **BCrypt fator 12** — senhas com custo computacional adequado
- **AES/GCM** — criptografia autenticada com IV aleatório por operação; dados do signatário protegidos em repouso
- **Rate Limiter** — por usuário e por IP (10 req/min)
- **Circuit Breaker** — proteção automática contra degradação do provider
- **Stateless** — sem sessão no servidor
- **Audit Log** — rastreabilidade de acessos e operações críticas com IP e User-Agent

---

## LGPD

| Requisito | Implementação |
|---|---|
| Proteção de dados em repouso | AES/GCM nos dados do signatário |
| Direito ao esquecimento | `DELETE /v1/users/me` — soft delete + anonimização |
| Registro de consentimento | Campo `consent_at` na tabela `users` |
| Rastreabilidade | `audit_log` com userId, action, resourceType, IP e User-Agent |
| Portabilidade | Timeline completa de eventos por envelope |

---

## Licença

Este projeto é proprietário Begotri Ltda. Todos os direitos reservados.

---

<div align="center">
  <sub>Construído com Spring Boot · PostgreSQL · Apache Kafka · Flyway · OpenFeign · Resilience4j · AES/GCM</sub>
</div>