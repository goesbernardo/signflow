# Análise de Viabilidade — Adaptação para Microserviços

**Branch:** `feature/adaptacao_estrutura_microservicos`  
**Data:** 2026-05-15  
**Baseado em:** varredura completa da codebase (194 arquivos Java, 10 tabelas, 4 tópicos Kafka)

---

## 1. Estado Atual da Arquitetura

O SignFlow é um **monólito modular** bem estruturado com Arquitetura Hexagonal (Ports & Adapters). Não é um monólito bagunçado — é um monólito com fronteiras de domínio claras.

```
signflow/                            LOC / Arquivos
├── api/                             Controllers + DTOs          (19 arquivos)
├── application/                     Services + Ports            (24 arquivos)
├── domain/                          Commands + Models           (18 arquivos)
├── config/                          Spring Config               (16 arquivos)
├── enums/                           Enums neutros               (11 arquivos)
└── infrastructure/
    ├── provider/clicksign/          Adapter ClickSign           (36 arquivos)
    ├── provider/docusign/           Adapter DocuSign            (31 arquivos)
    ├── persistence/                 JPA + Flyway                (30 arquivos)
    ├── gateway/                     Registry                    (1 arquivo)
    ├── security/                    AES/GCM                     (2 arquivos)
    └── exception/                   GlobalHandler               (5 arquivos)
```

### Acoplamento medido

| Componente | Dependências injetadas | Avaliação |
|---|---|---|
| `SignatureServiceImpl` | 13 (8 repos + 3 services + Kafka + HttpRequest) | ⚠️ Alto |
| `WebhookEventProcessor` | 4 (3 repos + Kafka) | ✅ Moderado |
| `ClickSignGateway` | 2 (Feign client + Mapper) | ✅ Baixo |
| `DocuSignGateway` | 2 (Feign client + Mapper) | ✅ Baixo |
| `WebhookConsumer` | 1 (Map de handlers) | ✅ Baixo |

### Modelo de dados — hub central

```
envelope_request (hub central)
    ├── signer          (FK → envelope_request)
    ├── document        (FK → envelope_request)
    ├── requirement     (FK → signer, document, envelope)
    ├── envelope_event  (FK → envelope_request, signer)
    ├── envelope_notifier (FK → envelope_request)
    └── outbound_webhook_delivery (FK → envelope_request)

users
    ├── provider_routing_rule
    ├── audit_log
    ├── login_attempt
    ├── mfa_code
    ├── password_history
    └── refresh_token
```

**Todas as 10 tabelas compartilham o mesmo schema PostgreSQL.**

---

## 2. O Projeto em Números

| Métrica | Valor |
|---|---|
| Arquivos Java | 194 |
| Tabelas no banco | 10 (core) + 6 (segurança) |
| Tópicos Kafka | 4 |
| Controllers | 7 |
| Repositories | 14 |
| Providers implementados | 2 (ClickSign: 36 arq, DocuSign: 31 arq) |
| Tempo de build (CI) | ~2-4 min (mvn clean package + test) |
| Ambientes | local (docker-compose) + prod (docker-compose.prod.yaml) |

---

## 3. Análise por Candidato a Microserviço

### 3.1 Provider Adapters (ClickSign / DocuSign)

**Pergunta: faz sentido cada provider ser um microserviço?**

| Fator | ClickSign separado | Avaliação |
|---|---|---|
| Independência de deploy | Os gateways não têm dados próprios — leem e escrevem na tabela `envelope_request` compartilhada | ❌ Não independente |
| Estado próprio | Nenhum — são adapters stateless | Neutro |
| Escalabilidade independente | ClickSign e DocuSign têm o mesmo padrão de carga (chamadas síncronas a providers externos) | ❌ Não justificado |
| Equipe dedicada | Improvável para uma integração de provider | ❌ Não justificado |
| Deploy frequência diferente | Providers mudam raramente (nova versão de API a cada 12-18 meses) | ❌ Não justificado |

**Veredicto:** Os providers SÃO naturalmente isolados na arquitetura atual — cada um tem seu próprio pacote, Feign client, DTOs, mapper, exception handler. O isolamento já existe. Criar um microserviço separado para cada provider adicionaria toda a complexidade distribuída sem benefício real, porque eles continuariam dependendo do banco compartilhado.

---

### 3.2 Webhook Processing Worker

**Este é o candidato mais natural para extração.**

| Fator | Avaliação |
|---|---|
| Já desacoplado via Kafka | ✅ WebhookConsumer consome de `signflow.webhook.received` |
| Escalabilidade independente | ✅ Picos de webhook são independentes do pico de API |
| Estado próprio | ⚠️ Ainda acessa `envelope_request`, `signer`, `envelope_event` |
| Complexidade de extração | Média — exigiria um banco de leitura ou API interna |

**Veredicto:** Tecnicamente viável como **worker separado** (não microserviço completo) — um processo Java que consome Kafka e acessa o banco via API interna ou réplica de leitura. Esforço: ~3-4 semanas.

---

### 3.3 Outbound Webhook Delivery

| Fator | Avaliação |
|---|---|
| Responsabilidade única | ✅ Envia webhooks para callbacks dos clientes + retry + DLQ |
| Estado próprio | ✅ Tabela `outbound_webhook_delivery` é quase autossuficiente |
| Escalabilidade independente | ✅ Volume de entregas pode crescer desacoplado do core |
| Esforço de extração | Baixo — `OutboundWebhookService` é o candidato mais limpo |

**Veredicto:** **Candidato legítimo** para extração futura. Pode virar um worker separado com acesso à tabela `outbound_webhook_delivery` via API ou banco próprio. Esforço: ~2-3 semanas.

---

### 3.4 Auth Service

| Fator | Avaliação |
|---|---|
| Reusabilidade | Só o SignFlow usa — não há outros serviços para consumir |
| Estado próprio | ✅ Tabelas: users, login_attempt, mfa_code, password_history, refresh_token |
| Complexidade de extração | Alta — JWT, MFA, LGPD, multi-tenant cortam transversalmente |
| Equipe dedicada | Não justificado no escopo atual |

**Veredicto:** Prematuro. O auth está correto como está. Extrair para um microserviço de identity agrega valor quando há múltiplos serviços consumindo autenticação.

---

## 4. O Custo Real da Migração

A tentação é focar no benefício dos microserviços. O custo é frequentemente subestimado.

### O que precisaria ser construído do zero

| Componente | Esforço | Justificativa |
|---|---|---|
| API Gateway (Kong, Traefik, Spring Cloud Gateway) | 2-3 semanas | Roteamento, auth, rate limit centralizado |
| Service Discovery (Consul / Kubernetes) | 2-3 semanas | Localização dinâmica de serviços |
| Distributed Tracing (Jaeger / Zipkin) | 1-2 semanas | Rastrear uma request por 4+ serviços |
| Banco por serviço (database-per-service) | 6-10 semanas | A parte mais difícil — quebrar o schema compartilhado |
| Saga / Compensating transactions | 4-8 semanas | `createEnvelope` hoje é uma transação local; distribuída exige Saga |
| Kubernetes manifests + Helm charts | 3-4 semanas | Replace do docker-compose atual |
| CI/CD por serviço | 3-4 semanas | Pipelines independentes por repo |
| Health checks, liveness, readiness probes | 1 semana | Por serviço |
| Shared libs / BOM | 2-3 semanas | Evitar duplicação de domain models |
| Contract testing (Pact, Spring Cloud Contract) | 2-3 semanas | Garantir compatibilidade entre serviços |

**Estimativa total de infra pura: 26-46 semanas** (sem contar a migração do código de negócio).

### O maior risco: o banco compartilhado

A tabela `envelope_request` é referenciada por 6 tabelas com FKs. Quebrar esse schema exigiria:
1. Escolher um padrão de decomposição (Strangler Fig, Event Sourcing, ou CQRS)
2. Criar APIs internas entre serviços para substituir JOINs SQL
3. Implementar eventual consistency onde hoje há transações ACID
4. Aceitar que uma operação que hoje é `@Transactional` torna-se uma Saga com rollback compensatório

Isso é uma reescrita parcial de regras de negócio — não uma refatoração de infraestrutura.

---

## 5. O Que Já Funciona Como "Proto-Microserviços"

O SignFlow já tem padrões que facilitariam uma futura migração:

| Padrão presente | Como ajuda |
|---|---|
| Kafka (4 tópicos) | Comunicação assíncrona já existe — extração de workers é incremental |
| Hexagonal Architecture | Cada provider é um adapter isolado — fronteiras de domínio claras |
| @ConditionalOnProperty nos Gateways | Providers podem ser habilitados/desabilitados independentemente |
| Circuit Breaker por provider | Cada provider já tem resiliência independente |
| Multi-tenant via TenantContext | Isolamento de dados já implementado |
| WebhookConsumer com Map<String, WebhookHandler> | Dispatch extensível sem modificar o consumer |

---

## 6. Recomendação: Evolução em 3 Fases

### Fase 1 — Consolidar o Monólito Modular (Agora, 0 esforço adicional)

O estado atual já é adequado. A única ação necessária é **reforçar as fronteiras de pacote** impedindo que código de `infrastructure/provider/clicksign` acesse `infrastructure/provider/docusign` diretamente.

```
Ação: adicionar ArchUnit ou PMD rules para enforce das fronteiras de pacote.
Esforço: 2-4 horas
```

### Fase 2 — Workers via Kafka (Quando houver gargalo de escala)

Extrair os consumers Kafka como **processos separados**, sem tocar no banco:

```
signflow-api           → Recebe requests, persiste no banco, publica no Kafka
signflow-webhook-in    → Consome signflow.webhook.received, processa, atualiza banco
signflow-webhook-out   → Consome signflow.webhook.outbound, entrega para callbacks, retry
```

Estes processos compartilham o banco (ou acessam via API interna), mas são deployados independentemente.

```
Esforço: 3-5 semanas
Pré-requisito: volume de webhooks > 1.000/min
```

### Fase 3 — Microserviços de Domínio (Quando a equipe e o volume justificarem)

Apenas quando:
- Equipe > 5 devs
- Providers com SLAs diferentes (um provider pode cair sem afetar o outro)
- Volume por provider > 10.000 envelopes/dia

```
Decomposição sugerida:
├── signflow-gateway        API Gateway (autenticação, routing, rate limit)
├── signflow-identity       Auth, MFA, usuários (tabelas users, mfa, refresh_token)
├── signflow-core           Envelopes, signers, documents (tabelas envelope_request e relacionadas)
├── signflow-routing        Smart routing rules (tabela provider_routing_rule)
├── signflow-webhook        Pipeline completo de webhooks
└── signflow-providers      Adapters ClickSign e DocuSign (stateless, acessa signflow-core via API)
```

```
Esforço: 30-50 semanas de time dedicado
```

---

## 7. Veredicto Final

| Pergunta | Resposta |
|---|---|
| Faz sentido migrar para microserviços agora? | **Não.** O custo de 26-50 semanas não é justificado pelo volume e tamanho de equipe atual. |
| Os providers (ClickSign, DocuSign) devem ser microserviços? | **Não.** Já estão isolados como adapters. Separar adicionaria complexidade distribuída sem benefício. |
| A arquitetura atual suporta crescimento? | **Sim.** O monólito hexagonal pode crescer para 5-10 providers sem refatoração. |
| Existe algo que faz sentido extrair agora? | **Sim.** O `OutboundWebhookService` é o candidato mais limpo para um worker separado no futuro. |
| O que fazer nesta feature branch? | **Documentar a decisão, reforçar fronteiras de pacote com ArchUnit, preparar o terreno para Fase 2.** |

### Regra prática

> Microserviços resolvem problemas de **escala de equipe** e **escala de volume**. Quando nenhum dos dois é o gargalo, microserviços adicionam complexidade sem retorno. O SignFlow, com sua arquitetura hexagonal e Kafka já em uso, está no ponto ideal para crescer como monólito modular até que um problema real de escala justifique a extração.

---

## 8. Ações Recomendadas para Esta Feature Branch

1. **Adicionar ArchUnit** para enforçar que providers não se cruzam
2. **Preparar `signflow-webhook-worker`** como módulo Maven separado (sem extração ainda — apenas estrutura)
3. **Documentar o contrato** entre o monólito e futuros workers via schema Avro/JSON dos tópicos Kafka
4. **Adicionar comentários de fronteira** nos pacotes de provider indicando os limites de extração futura
