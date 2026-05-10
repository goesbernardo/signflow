# SignFlow

> **Gateway de Assinatura Eletrônica Multi-Provider**  
> Uma API unificada que abstrai múltiplos provedores de assinatura eletrônica — integre uma vez, use qualquer provider.

---

## Visão Geral

O SignFlow é uma plataforma backend construída com **Spring Boot** que funciona como camada de abstração entre sua aplicação e os provedores de assinatura eletrônica do mercado.

Em vez de integrar cada provedor de forma isolada — lidando com APIs diferentes, autenticações diferentes e webhooks em formatos diferentes — você integra o SignFlow uma única vez e ganha acesso a todos eles com uma API padronizada.

### Por que o SignFlow?

| Problema atual | O que o SignFlow resolve |
|---|---|
| Lock-in com um único provider | Troque de provider sem alterar uma linha de código |
| Múltiplas integrações para múltiplos providers | Uma API, uma autenticação, um formato de webhook |
| Sem controle de custo por provider | Smart routing automático por regras configuráveis |
| Provider caiu, contratos parados | Fallback automático para provider secundário |
| Dois painéis para dois providers | Dashboard e auditoria unificados |

---

## Funcionalidades

- **Fluxo completo em uma chamada** — crie envelope, adicione documentos, configure signatários e ative em um único `POST`
- **7 métodos de autenticação** — E-mail, SMS, WhatsApp, Pix, Assinatura Manuscrita, Biometria Facial e API
- **Aceite via WhatsApp** — formalização de acordos sem documento PDF
- **Smart Routing** — seleção automática de provider por regras configuráveis (`ALWAYS`, `AUTH_METHOD`)
- **Webhook normalizado** — recebimento e processamento de eventos de qualquer provider em formato único
- **Webhook de saída** — notificação em tempo real para a URL do cliente com retry automático (3 tentativas com backoff)
- **Observadores** — gestores acompanham o envelope sem precisar assinar
- **Lembrete manual** — renotificação de signatários com rate limit de 1/hora
- **Cancelamento e ativação manual** — controle total do ciclo de vida do envelope
- **Auditoria completa** — timeline de eventos com origem (API ou WEBHOOK), evento do provider e signatário responsável

---

## Arquitetura

O projeto segue os princípios da **Arquitetura Hexagonal** (Ports and Adapters):

```
signflow/
├── api/                        # Controllers e DTOs (entrada)
├── application/
│   ├── port/
│   │   ├── in/                 # Interfaces de entrada (SignatureService)
│   │   └── out/                # Interfaces de saída (ESignatureGateway)
│   ├── service/                # Implementações de negócio
│   └── webhook/                # Processamento de eventos
├── domain/
│   ├── command/                # Objetos de comando (neutros de provider)
│   ├── exception/              # Exceções de domínio com códigos semânticos
│   └── model/                  # Modelos de domínio
├── enums/                      # Enums neutros (SignatureAuthMethod, SignerRole...)
└── infrastructure/
    ├── exception/              # Tratamento global de erros
    ├── gateway/                # Registry de providers
    ├── persistence/            # Entities, Repositories, Migrations
    └── provider/
        └── clicksign/          # Implementação ClickSign (gateway, mapper, webhook)
```

### Princípio central

Os `Commands` usam enums neutros de domínio — `SignatureAuthMethod`, `SignerRole`, `NotificationChannel`. Cada gateway mapeia esses enums para os tipos específicos do seu provider sem contaminar o domínio.

---

## Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Runtime | Java 17 |
| Framework | Spring Boot 3.x |
| Segurança | Spring Security + JWT HS512 |
| Banco de dados | PostgreSQL |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| HTTP Client | OpenFeign |
| Resiliência | Resilience4j (Circuit Breaker + Rate Limiter) |
| Documentação | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |
| Deploy | Render |

---

## Providers Suportados

| Provider | Status | Autenticações suportadas |
|---|---|---|
| ClickSign | ✅ Implementado | EMAIL, SMS, WhatsApp, Pix, Manuscrita, Biometria Facial, API |
| D4Sign | 🔜 Em desenvolvimento | — |
| ZapSign | 📋 Mapeado | — |
| DocuSign | 📋 Mapeado | — |

---

## Endpoints

A documentação interativa completa está disponível via Swagger UI após o deploy.

### Autenticação
```
POST /api/v1/auth/login
```

### Envelopes
```
GET    /api/v1/signatures                          # Listar (paginado, filtro por status)
GET    /api/v1/signatures/{id}                     # Buscar (?includeSigners=true)
PATCH  /api/v1/signatures/{id}                     # Editar nome
GET    /api/v1/signatures/{id}/timeline            # Auditoria completa
POST   /api/v1/signatures/create-activate-envelope # Fluxo completo
POST   /api/v1/signatures/{id}/activate            # Ativar rascunho
POST   /api/v1/signatures/{id}/cancel              # Cancelar
POST   /api/v1/signatures/{id}/notifiers           # Adicionar observador
GET    /api/v1/signatures/{id}/webhook-deliveries  # Histórico de callbacks
```

### Signatários
```
GET    /api/v1/signatures/{id}/signers
GET    /api/v1/signatures/{id}/signers/{signerId}
DELETE /api/v1/signatures/{id}/signers/{signerId}
POST   /api/v1/signatures/{id}/signers/{signerId}/remind
```

### Documentos e Requisitos
```
GET/PATCH/DELETE /api/v1/signatures/documents/{id}
GET              /api/v1/signatures/{id}/documents
GET/DELETE       /api/v1/signatures/requirements/{id}
GET              /api/v1/signatures/{id}/requirements
```

### Smart Routing
```
GET    /api/v1/routing-rules
POST   /api/v1/routing-rules
PUT    /api/v1/routing-rules/{id}
DELETE /api/v1/routing-rules/{id}
```

### WhatsApp e Webhook
```
POST /api/v1/whatsapp/acceptance
POST /api/v1/webhook/{provider}
```

---

## Schema do Banco

O banco é gerenciado inteiramente pelo Flyway. As principais tabelas:

| Tabela | Descrição |
|---|---|
| `users` | Usuários da plataforma com role e status |
| `envelope_request` | Envelopes com status interno e do provider |
| `signer` | Signatários com status, `signed_at` e `ip_address` |
| `document` | Documentos associados ao envelope |
| `requirement` | Requisitos de qualificação e autenticação |
| `envelope_event` | Auditoria completa: origem, evento do provider, signatário |
| `envelope_notifier` | Observadores do envelope |
| `outbound_webhook_delivery` | Histórico de entregas de callback com retry |
| `provider_routing_rule` | Regras de smart routing por usuário |

---

## Configuração

### Pré-requisitos

- Java 17+
- PostgreSQL 14+
- Maven 3.8+

### Variáveis de ambiente

Crie um arquivo `.env` baseado no exemplo abaixo. **Nunca commite credenciais reais.**

```env
# Banco de dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/signflow
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha

# JWT — mínimo 64 caracteres (512 bits para HS512)
# gere com: openssl rand -hex 64
JWT_SECRET=seu_jwt_secret_de_no_minimo_64_caracteres_aqui

# ClickSign
CLICKSIGN_URL=https://sandbox.clicksign.com/api/v3
CLICKSIGN_API_TOKEN=seu_token_clicksign
CLICKSIGN_WEBHOOK_SECRET=seu_webhook_secret

# Swagger
SWAGGER_SERVER_URL=http://localhost:8080
```

### Executando localmente

```bash
# Clonar o repositório
git clone https://github.com/goesbernardo/signflow.git
cd signflow

# Subir o banco com Docker
docker-compose up -d

# Executar a aplicação
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

A aplicação estará disponível em `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Fluxo de Assinatura

### Fluxo completo em uma chamada

```bash
curl -X POST http://localhost:8080/api/v1/signatures/create-activate-envelope \
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

### Métodos de autenticação disponíveis

| Valor | Descrição | Requisitos |
|---|---|---|
| `EMAIL` | Token por e-mail | — |
| `SMS` | Token por SMS | `phone_number` obrigatório |
| `WHATSAPP` | Token por WhatsApp | `phone_number` obrigatório |
| `PIX` | Pagamento Pix R$ 0,01 | `documentation` (CPF) obrigatório |
| `HANDWRITTEN` | Assinatura manuscrita | — |
| `FACIAL_BIOMETRICS` | Reconhecimento facial | `documentation` (CPF) obrigatório |
| `API` | Programática | — |

### Papéis do signatário

`SIGN` · `PARTY` · `CONTRACTOR` · `WITNESS` · `INTERVENING`

---

## Smart Routing

Configure regras para que o SignFlow selecione o provider automaticamente:

```bash
# Sempre usar ClickSign (provider padrão)
POST /api/v1/routing-rules
{
  "priority": 1,
  "conditionType": "ALWAYS",
  "provider": "CLICKSIGN",
  "active": true
}

# Usar D4Sign quando o método de auth for PIX
POST /api/v1/routing-rules
{
  "priority": 2,
  "conditionType": "AUTH_METHOD",
  "conditionValue": "PIX",
  "provider": "D4SIGN",
  "active": true
}
```

Quando o header `provider` não for informado, o SignFlow avalia as regras em ordem de prioridade e seleciona automaticamente.

---

## Segurança

- Autenticação via **JWT HS512** com expiração configurável
- Senhas com **BCrypt fator 12**
- **Rate Limiting** por usuário e por IP
- **Circuit Breaker** para proteção contra falhas do provider
- Sessão **stateless** — sem estado no servidor
- Rastreabilidade completa com **requestId** e **userId** em todos os logs
- Rotas públicas restritas: apenas `/auth/login`, `/webhook/**`, `/swagger-ui/**` e `/actuator/health`

---

## Rastreabilidade e Auditoria

Cada operação gera um evento rastreável na tabela `envelope_event`:

```
source: API | WEBHOOK
provider_event: sign | cancel | close | deadline | refusal | add_signer
provider_status: running | completed | canceled | draft
signer_id: quem executou a ação
occurred_at: timestamp exato
```

Todos os logs incluem `requestId` (UUID por requisição) e `userId` (usuário autenticado).

---

## Webhook de Saída

Configure `callbackUrl` no envelope para receber notificações em tempo real:

```json
{
  "envelopeId": "uuid-do-envelope",
  "provider": "CLICKSIGN",
  "eventType": "DOCUMENT_COMPLETED",
  "status": "CLOSED",
  "occurredAt": "2026-05-10T14:30:00Z"
}
```

O sistema tenta a entrega 3 vezes com backoff progressivo (0s → 60s → 600s). O histórico de entregas está disponível em `GET /signatures/{id}/webhook-deliveries`.

---

## Deploy

O projeto está configurado para deploy no **Render**.

Consulte o `application-prod.yml` e configure as variáveis de ambiente listadas na seção de configuração. O Flyway aplica as migrations automaticamente no startup.

---

## Contribuição

1. Fork o repositório
2. Crie uma branch: `git checkout -b feature/minha-feature`
3. Commit suas mudanças: `git commit -m 'feat: descrição da feature'`
4. Push para a branch: `git push origin feature/minha-feature`
5. Abra um Pull Request

---

## Licença

Este projeto é proprietário. Todos os direitos reservados.

---

<div align="center">
  <sub>Construído com Spring Boot · PostgreSQL · Flyway · OpenFeign · Resilience4j</sub>
</div>