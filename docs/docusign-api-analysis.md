# Análise DocuSign eSign REST API v2.1 — Mapeamento para SignFlow

**Branch:** `implementacao-docusign-provider`  
**Referência:** https://developers.docusign.com/docs/esign-rest-api/reference/  
**Data:** 2026-05-15

---

## 1. Inventário Completo das APIs DocuSign — Status de Implementação

### 1.1 Envelopes API

| Endpoint | Método | Descrição | Status SignFlow |
|---|---|---|---|
| `/accounts/{id}/envelopes` | POST | Criar envelope (envio remoto, embedded, draft) | ✅ Implementado (`createEnvelope`) |
| `/accounts/{id}/envelopes/{envId}` | GET | Buscar envelope e status | ✅ Implementado (`getEnvelope`) |
| `/accounts/{id}/envelopes/{envId}` | PUT | Atualizar envelope (nome, status, void) | ✅ Implementado (`updateEnvelope`) |
| `/accounts/{id}/envelopes` | GET | Listar envelopes com filtros | ✅ Via banco local |
| `/accounts/{id}/envelopes/{envId}/documents` | GET | Listar documentos do envelope | ✅ Implementado |
| `/accounts/{id}/envelopes/{envId}/documents` | PUT | Adicionar documentos | ✅ Implementado (`addDocument`) |
| `/accounts/{id}/envelopes/{envId}/documents/{docId}` | GET | **Download documento assinado (PDF)** | ❌ Não implementado |
| `/accounts/{id}/envelopes/{envId}/recipients` | GET | Listar signatários | ✅ Implementado |
| `/accounts/{id}/envelopes/{envId}/recipients` | POST | Adicionar signatário | ✅ Implementado (`addSigner`) |
| `/accounts/{id}/envelopes/{envId}/recipients` | PUT | Reenviar para signatários | ✅ Via `remindSigner` |
| `/accounts/{id}/envelopes/{envId}/recipients/{recId}/tabs` | POST | Adicionar tabs/campos | ✅ Implementado (`addRequirement`) |
| `/accounts/{id}/envelopes/{envId}/recipients/{recId}/tabs` | GET | Listar tabs do signatário | ❌ Não implementado |
| `/accounts/{id}/envelopes/{envId}/audit_events` | GET | **Audit trail completo** | ❌ Não implementado |
| `/accounts/{id}/envelopes/{envId}/notification` | GET/PUT | Lembretes e expiração | ❌ Não implementado |

### 1.2 Views API — Embedded Signing ⭐ PRIORITÁRIO

| Endpoint | Método | Descrição | Status SignFlow |
|---|---|---|---|
| `/accounts/{id}/envelopes/{envId}/views/recipient` | POST | **URL de embedded signing para signatário** | ❌ **NÃO IMPLEMENTADO** |
| `/accounts/{id}/envelopes/{envId}/views/sender` | POST | **URL de embedded sending para remetente** | ❌ **NÃO IMPLEMENTADO** |
| `/accounts/{id}/envelopes/{envId}/views/correct` | POST | URL para corrigir envelope | ❌ Não implementado |
| `/accounts/{id}/views/console` | POST | DocuSign Console embedded no sistema | ❌ Não implementado |

### 1.3 Templates API ⭐ PRIORITÁRIO

| Endpoint | Método | Descrição | Status SignFlow |
|---|---|---|---|
| `/accounts/{id}/templates` | POST | Criar template reutilizável | ❌ **NÃO IMPLEMENTADO** |
| `/accounts/{id}/templates` | GET | Listar templates | ❌ **NÃO IMPLEMENTADO** |
| `/accounts/{id}/templates/{templateId}` | GET | Buscar template | ❌ Não implementado |
| `/accounts/{id}/templates/{templateId}` | PUT | Atualizar template | ❌ Não implementado |
| `/accounts/{id}/templates/{templateId}` | DELETE | Deletar template | ❌ Não implementado |
| `/accounts/{id}/templates/{templateId}/recipients` | GET | Roles do template | ❌ Não implementado |
| `/accounts/{id}/envelopes` + `templateId` | POST | Criar envelope a partir de template | ❌ Não implementado |

### 1.4 Bulk Send API ⭐ DIFERENCIADOR

| Endpoint | Método | Descrição | Status SignFlow |
|---|---|---|---|
| `/accounts/{id}/bulk_send_lists` | POST | Criar lista de destinatários (até 1000) | ❌ **NÃO IMPLEMENTADO** |
| `/accounts/{id}/bulk_send_lists/{bulkSendListId}` | GET | Consultar lista | ❌ Não implementado |
| `/accounts/{id}/bulk_send_batch/{batchId}` | GET | Status do envio em massa | ❌ Não implementado |
| `/accounts/{id}/envelopes/{envId}/bulk_send_batch` | POST | Disparar envio em massa | ❌ Não implementado |

### 1.5 Connect (Webhooks) — Eventos em Tempo Real

| Evento | Descrição | Status SignFlow |
|---|---|---|
| `envelope-sent` | Envelope enviado | ✅ Mapeado → `DOCUMENT_SENT` |
| `envelope-delivered` | Visualizado pelo signatário | ✅ Mapeado → `DOCUMENT_VIEWED` |
| `envelope-signed` | Assinado parcialmente | ✅ Mapeado → `DOCUMENT_SIGNED` |
| `envelope-completed` | Todos assinaram | ✅ Mapeado → `DOCUMENT_COMPLETED` |
| `envelope-declined` | Recusado | ✅ Mapeado → `DOCUMENT_REJECTED` |
| `envelope-voided` | Cancelado | ✅ Mapeado → `DOCUMENT_CANCELED` |
| `recipient-sent` | Notificação enviada ao signatário | ✅ Mapeado → `DOCUMENT_SENT` |
| `recipient-completed` | Signatário específico completou | ✅ Mapeado → `DOCUMENT_SIGNED` |
| `recipient-declined` | Signatário recusou | ✅ Mapeado → `DOCUMENT_REJECTED` |
| **Embedded signing events** | Eventos via `returnUrl` (signing complete, declined, session timeout) | ❌ Não tratado |

### 1.6 Tabs (Campos Dinâmicos) — Tipos Suportados

| Tipo de Tab | DocuSign API Field | Mapeado no SignFlow |
|---|---|---|
| `signHereTabs` | Campo de assinatura | ✅ Implementado |
| `initialHereTabs` | Campo de rubrica | ✅ Implementado |
| `dateSignedTabs` | Data automática de assinatura | ✅ Na estrutura |
| `textTabs` | Campo de texto livre | ❌ Não exposto |
| `checkboxTabs` | Checkbox | ❌ Não exposto |
| `radioGroupTabs` | Radio buttons | ❌ Não exposto |
| `numberTabs` | Campo numérico | ❌ Não exposto |
| `emailTabs` | Campo de email validado | ❌ Não exposto |
| `approveTabs` | Botão de aprovação | ❌ Não exposto |
| `declineTabs` | Botão de recusa | ❌ Não exposto |
| `fullNameTabs` | Nome completo automático | ❌ Não exposto |
| `formulaTab` | Campo com fórmula calculada | ❌ Não exposto |
| `attachmentTabs` | Upload de anexo pelo signatário | ❌ Não exposto |

### 1.7 Autenticação de Signatários (Identity Verification)

| Método | DocuSign Feature | Status SignFlow |
|---|---|---|
| Email OTP | `emailNotification` + `accessCode` | ✅ Mapeado (`EMAIL`) |
| SMS OTP | `phoneAuthentication` | ✅ Parcial (delivery method SMS) |
| Access Code | `accessCode` field no recipient | ❌ Não exposto |
| Knowledge-Based Auth (KBA) | `kbaAuthentication` | ❌ Não implementado |
| ID Verification (ID+selfie) | `identityVerification` | ❌ Não implementado |
| Biometria facial | `identityVerification` + biometric | ❌ Não implementado |

### 1.8 Audit Trail

| Feature | API | Status |
|---|---|---|
| Eventos por envelope | `GET /envelopes/{id}/audit_events` | ❌ Não implementado |
| Certificate of Completion (PDF) | `GET /envelopes/{id}/documents/certificate` | ❌ Não implementado |
| Histórico de transações | Via Connect + eventos locais | ✅ Parcial (webhook pipeline) |

### 1.9 Gestão Documental

| Feature | API | Status |
|---|---|---|
| Download PDF assinado | `GET /envelopes/{id}/documents/{docId}` | ❌ **NÃO IMPLEMENTADO** |
| Download todos os docs (ZIP) | `GET /envelopes/{id}/documents/archive` | ❌ Não implementado |
| Rotação de página | `pages` array no documento | ❌ Não implementado |
| Múltiplos formatos | PDF, DOCX, imagem | Parcial (só PDF testado) |

---

## 2. Priorização por Valor de Negócio

### Tier 1 — CRÍTICO (implementar agora, diferenciadores imediatos)

#### 🥇 Embedded Signing
**Por que é o mais importante:**
- Evita que o usuário saia da plataforma SignFlow
- Diferencial SaaS white-label — a empresa cliente assina dentro do próprio sistema
- Aumenta taxa de conversão de assinatura (menos abandono)
- Exigido para contratos de alto valor (B2B enterprise)

**Como funciona:**
```
1. POST /envelopes                    → cria envelope com status "created"
2. POST /envelopes/{id}/views/recipient → retorna signingUrl (válida por 5 min)
3. SPA renderiza signingUrl em iframe ou redirect
4. DocuSign redireciona para returnUrl ao finalizar
5. returnUrl recebe: event=signing_complete|decline|session_timeout|ttl_expired
```

**Esforço estimado:** 1-2 semanas  
**Endpoint novo:** `POST /v1/signatures/{envelopeId}/views/signing`

---

#### 🥇 Download de Documento Assinado
**Por que é crítico:**
- Sem isso, o cliente não consegue recuperar o PDF assinado pelo SignFlow
- Exigido para compliance (LGPD, eIDAS, ESIGN)
- Mais solicitado em qualquer plataforma de assinatura

**Como funciona:**
```
GET /accounts/{id}/envelopes/{envId}/documents/{docId}
→ retorna stream de bytes do PDF
→ Content-Type: application/pdf
```

**Esforço estimado:** 3-5 dias  
**Endpoint novo:** `GET /v1/signatures/{envelopeId}/documents/{docId}/download`

---

### Tier 2 — ALTO VALOR (implementar no próximo ciclo)

#### 🥈 Template Engine
**Por que é estratégico:**
- Empresas assinam o mesmo contrato centenas de vezes (RH, locação, serviços)
- Reduz tempo de envio de minutos para segundos
- Posiciona o SignFlow como solução enterprise
- Templates são multi-provider → o SignFlow pode ter templates que funcionam com ClickSign OU DocuSign

**Como funciona:**
```
1. POST /accounts/{id}/templates      → cria template com docs, tabs, roles
2. POST /accounts/{id}/envelopes + {"templateId": "...", "templateRoles": [...]}
   → cria envelope instantâneo a partir do template
```

**Esforço estimado:** 2-3 semanas

---

#### 🥈 Audit Trail Completo
**Por que é estratégico:**
- Compliance jurídico — exigido para contratos com valor legal
- Diferenciador para segmento financeiro, jurídico, saúde
- Integra com o pipeline de `timeline` já existente no SignFlow

**Como funciona:**
```
GET /accounts/{id}/envelopes/{envId}/audit_events
→ retorna IP, timestamp, geolocalização, dispositivo, hash por ação

GET /accounts/{id}/envelopes/{envId}/documents/certificate
→ PDF com toda a cadeia de custódia (Certificate of Completion)
```

**Esforço estimado:** 1 semana

---

#### 🥈 Campos Dinâmicos Expandidos (Tabs)
**Por que é estratégico:**
- CPF, checkbox de aceite, data, campo de texto são exigidos em contratos reais
- Hoje o SignFlow só expõe `signHere` e `initialHere`
- Expandir para 6-8 tipos de tab cobre 95% dos casos de uso

**Novos tipos para implementar:**
- `textTabs` — informações adicionais do signatário
- `checkboxTabs` — aceite de termos
- `radioGroupTabs` — escolha de opção
- `numberTabs` — valor de contrato
- `attachmentTabs` — upload de documento pelo signatário
- `approveTabs` / `declineTabs` — fluxo de aprovação

**Esforço estimado:** 1-2 semanas

---

### Tier 3 — DIFERENCIADOR PREMIUM (roadmap 6 meses)

#### 🥉 Bulk Send
**Por que é valioso:**
- RH: envio de contrato de trabalho para centenas de funcionários
- Fintechs: aceite de termos em massa
- Onboarding de parceiros em campanhas

**Limitações DocuSign:** máximo 1.000 destinatários por batch, requer plano Business Pro+

**Esforço estimado:** 2-3 semanas

---

#### 🥉 Embedded Sender View
**Por que é valioso:**
- O operador configura o envelope DENTRO do SignFlow (sem ir ao console DocuSign)
- White-label completo — o cliente nunca vê o DocuSign
- Casos de uso: preparação de contrato com campo de arrastar-e-soltar

**Esforço estimado:** 1-2 semanas

---

#### 🥉 ID Verification (KBA + Biometria)
**Por que é valioso:**
- Contratos de alto valor jurídico (imobiliário, financeiro)
- Exigência regulatória em alguns setores
- Diferencial competitivo frente ao ClickSign no mercado enterprise

**Complexidade:** Alta — requer DocuSign ID Verification add-on (custo extra)

**Esforço estimado:** 3-4 semanas

---

## 3. Mapeamento Gap Analysis — O Que Falta Implementar

```
Implementado (✅)
├── createEnvelope
├── getEnvelope
├── updateEnvelope
├── addSigner (recipients)
├── addDocument
├── addRequirement (signHere + initialHere tabs)
├── addNotifier (carbon copy)
├── cancelEnvelope (void)
├── activateEnvelope (send)
├── remindSigner (resend)
├── webhooks (Connect) — 9 eventos mapeados
└── OAuth2 JWT Bearer Grant

Não implementado (❌) — por prioridade
├── CRÍTICO
│   ├── POST /views/recipient       → Embedded Signing URL
│   └── GET /documents/{id}        → Download PDF assinado
│
├── ALTO VALOR
│   ├── POST /templates             → Criar template
│   ├── GET /templates              → Listar templates
│   ├── POST /envelopes (templateId)→ Criar envelope de template
│   ├── GET /audit_events           → Audit trail
│   ├── GET /documents/certificate  → Certificate of Completion
│   └── Tabs expandidos (text, checkbox, radio, number, attachment)
│
└── DIFERENCIADOR PREMIUM
    ├── POST /views/sender          → Embedded Sender View
    ├── POST /bulk_send_lists       → Bulk Send
    ├── GET /bulk_send_batch/{id}   → Status do bulk
    ├── kbaAuthentication           → KBA auth
    ├── identityVerification        → ID+selfie
    └── GET /envelopes/{id}/recipients/{id}/tabs → Listar tabs
```

---

## 4. Novos Endpoints SignFlow Propostos

```yaml
# Embedded Signing
POST /v1/signatures/{envelopeId}/views/signing
  body: { returnUrl, recipientEmail, recipientName, clientUserId }
  response: { signingUrl, expiresAt }

# Embedded Sender View
POST /v1/signatures/{envelopeId}/views/sender
  body: { returnUrl }
  response: { senderUrl, expiresAt }

# Download documento
GET /v1/signatures/{envelopeId}/documents/{documentId}/download
  headers: provider (optional)
  response: application/pdf stream

# Download Certificate of Completion
GET /v1/signatures/{envelopeId}/certificate
  response: application/pdf stream

# Audit trail DocuSign
GET /v1/signatures/{envelopeId}/audit
  response: [{ eventType, userId, deviceType, ip, latitude, longitude, timestamp }]

# Templates
POST /v1/templates
  body: { name, description, documents, recipients, tabs }
  response: { templateId, name, createdAt }

GET /v1/templates
  response: Page<{ templateId, name, created, lastUsed }>

POST /v1/signatures/create-from-template
  body: { templateId, signers: [{ role, name, email }], autoActivate }
  response: Envelope

# Bulk Send
POST /v1/signatures/bulk-send
  body: { templateId, recipients: [{ name, email, customFields }] }
  response: { batchId, totalCount, queuedAt }

GET /v1/signatures/bulk-send/{batchId}/status
  response: { total, sent, failed, pending }

# Campos expandidos (tabs)
# Adicionados ao CreateFullEnvelopeCommand.requirements[]:
# type: SIGN | INITIAL | DATE | TEXT | CHECKBOX | RADIO | NUMBER | ATTACHMENT | APPROVE | DECLINE
```

---

## 5. Arquitetura Proposta para Embedded Signing

```
Cliente SaaS
    │ 1. POST /v1/signatures/{envelopeId}/views/signing
    ▼
SignFlow API
    │ 2. GET DocuSign recipient view URL
    ▼
DocuSign API → retorna { url: "https://demo.docusign.net/Signing/..." }
    │ 3. SignFlow retorna { signingUrl, expiresAt }
    ▼
Cliente SaaS
    │ 4. Abre signingUrl em iframe/popup
    ▼
DocuSign Embedded Signing (dentro do browser do usuário)
    │ 5. Usuário assina
    ▼
DocuSign redireciona para returnUrl?event=signing_complete
    │ 6. SignFlow processa o evento
    ▼
Connect Webhook → Kafka → WebhookEventProcessor → status CLOSED
```

**Segurança:** O `clientUserId` vincula o signatário à URL de assinatura. Só o usuário autenticado no SignFlow pode iniciar o embedded signing do seu envelope.

---

## 6. Impacto por Segmento de Mercado

| Feature | Jurídico | RH | Financeiro | Imobiliário | SaaS |
|---|---|---|---|---|---|
| Embedded Signing | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| Download PDF assinado | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| Templates | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| Audit Trail | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| Tabs expandidos | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| Bulk Send | ⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐ | ⭐⭐⭐ |
| ID Verification | ⭐⭐⭐ | ⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐ |
| Embedded Sender | ⭐⭐ | ⭐⭐ | ⭐⭐ | ⭐⭐ | ⭐⭐⭐ |

---

## 7. Roadmap de Implementação

```
Sprint 1 (2 semanas) — Completar o essencial
├── Download de documento assinado (GET /documents/{id})
├── Embedded Signing URL (POST /views/recipient)
└── Audit trail (GET /audit_events)

Sprint 2 (2 semanas) — Campos e autenticação
├── Tabs expandidos: text, checkbox, number, attachment
├── Access Code authentication
└── Certificate of Completion download

Sprint 3 (3 semanas) — Template Engine
├── CRUD de templates DocuSign
├── Criar envelope a partir de template
└── Sincronização de templates com banco local

Sprint 4 (2 semanas) — Premium
├── Embedded Sender View
├── Bulk Send (lista + disparo + status)
└── Notificações e expiração (envelope notification settings)
```

**Total: ~9 semanas** para implementação completa do Tier 1+2+3.
