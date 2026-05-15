# Relatório de Testes QA — SignFlow

**Data:** 2026-05-15  
**Aplicação:** http://localhost:8081  
**Banco de dados:** postgresql://root:root@localhost:5432/signflow  
**Executor:** Claude Code (QA Sênior automatizado)

---

## Mapeamento de Endpoints

| Método | Endpoint | Autenticação |
|--------|----------|--------------|
| POST | `/v1/auth/login` | Pública |
| POST | `/v1/auth/mfa/verify` | Token MFA |
| POST | `/v1/auth/refresh` | Pública |
| POST | `/v1/auth/logout` | Pública |
| GET | `/v1/admin/users` | Bearer + ADMIN |
| POST | `/v1/admin/users` | Bearer + ADMIN |
| DELETE | `/v1/users/me` | Bearer |
| POST | `/v1/users/change-password` | Bearer |
| GET | `/v1/signatures` | Bearer |
| GET | `/v1/signatures/{externalId}` | Bearer |
| PATCH | `/v1/signatures/{externalId}` | Bearer |
| GET | `/v1/signatures/{externalId}/timeline` | Bearer |
| POST | `/v1/signatures/create-activate-envelope` | Bearer |
| POST | `/v1/signatures/{externalId}/cancel` | Bearer |
| POST | `/v1/signatures/{externalId}/activate` | Bearer |
| GET | `/v1/signatures/{externalId}/documents` | Bearer |
| GET | `/v1/signatures/documents/{documentId}` | Bearer |
| PATCH | `/v1/signatures/documents/{documentId}` | Bearer |
| DELETE | `/v1/signatures/documents/{documentId}` | Bearer |
| GET | `/v1/signatures/{externalId}/signers` | Bearer |
| GET | `/v1/signatures/{externalId}/signers/{signerId}` | Bearer |
| DELETE | `/v1/signatures/{externalId}/signers/{signerId}` | Bearer |
| GET | `/v1/signatures/{externalId}/requirements` | Bearer |
| GET | `/v1/signatures/requirements/{requirementId}` | Bearer |
| DELETE | `/v1/signatures/requirements/{requirementId}` | Bearer |
| POST | `/v1/signatures/{envelopeId}/signers/{signerId}/remind` | Bearer |
| POST | `/v1/signatures/{envelopeId}/notifiers` | Bearer |
| GET | `/v1/signatures/{externalId}/webhook-deliveries` | Bearer |
| GET | `/v1/routing-rules` | Bearer |
| POST | `/v1/routing-rules` | Bearer |
| PUT | `/v1/routing-rules/{id}` | Bearer |
| DELETE | `/v1/routing-rules/{id}` | Bearer |
| POST | `/v1/webhook/{provider}` | Pública |
| POST | `/v1/whatsapp/acceptance` | Bearer |

---

## Fluxo de Autenticação

### TC-001 — POST /v1/auth/login (credenciais válidas)
- **Status esperado:** 200  
- **Status retornado:** 200 ✅
- **Request:**
  ```json
  { "username": "admin", "password": "admin123" }
  ```
- **Response:**
  ```json
  {
    "accessToken": null,
    "refreshToken": null,
    "mfaToken": "eyJhbGciOiJIUzM4NCJ9...",
    "mfaRequired": true
  }
  ```

---

### TC-002 — SELECT code FROM mfa_codes (consulta ao banco)
- **Resultado:** Código `817423` obtido com sucesso via `docker exec signflow-db psql`

---

### TC-003 — POST /v1/auth/mfa/verify (código MFA válido)
- **Status esperado:** 200  
- **Status retornado:** 200 ✅
- **Request:**
  ```json
  { "mfaToken": "eyJhbGciOiJIUzM4NCJ9...", "code": "817423" }
  ```
- **Response:**
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "fabbe9b5-7982-4716-8905-958a8c078ec7",
    "mfaToken": null,
    "mfaRequired": false
  }
  ```
- **JWT obtido** e usado nos testes subsequentes.

---

### TC-004 — POST /v1/auth/refresh (refresh token válido)
- **Status esperado:** 200  
- **Status retornado:** 200 ✅
- **Request:**
  ```json
  { "refreshToken": "fabbe9b5-7982-4716-8905-958a8c078ec7" }
  ```
- **Response:** Novo `accessToken` e mesmo `refreshToken` retornados.

---

### TC-005 — POST /v1/auth/logout
- **Status esperado:** 204  
- **Status retornado:** 204 ✅
- **Request:**
  ```json
  { "refreshToken": "fabbe9b5-7982-4716-8905-958a8c078ec7" }
  ```
- **Response:** Body vazio.

---

## Endpoints Autenticados

### TC-006 — GET /v1/admin/users (usuário ADMIN)
- **Status esperado:** 200  
- **Status retornado:** 200 ✅
- **Response:** Lista com 4 usuários (`admin`, `usuario.teste`, `usuario.teste001`, `usuarioteste1505`, `qa.test.user`).
- **Observação:** E-mails de usuários aparecem criptografados (AES/GCM) na listagem, exceto para o admin.

---

### TC-007 — POST /v1/admin/users (criar usuário via admin)
- **Status esperado:** 201  
- **Status retornado:** 201 ✅
- **Request:**
  ```json
  {
    "username": "qa.test.user",
    "email": "qa_test@signflow.com",
    "password": "Test@12345678",
    "name": "QA Test User",
    "tenantId": "SYSTEM",
    "role": "OPERATOR"
  }
  ```
- **Response:**
  ```json
  { "name": "QA Test User", "role": "OPERATOR", "message": "usuário criado com sucesso na plataforma signflow" }
  ```

---

### TC-008 — GET /v1/signatures (listagem paginada)
- **Status esperado:** 200  
- **Status retornado:** 200 ✅
- **Response:** Página vazia (`totalElements: 0`). Paginação e ordenação funcionando corretamente.

---

### TC-009 — GET /v1/signatures?includeSigners=true&page=0&size=5
- **Status esperado:** 200  
- **Status retornado:** 200 ✅
- **Response:** Página vazia com `size: 5` e `includeSigners` processado sem erro.

---

### TC-010 — GET /v1/routing-rules
- **Status esperado:** 200  
- **Status retornado:** 200 ✅
- **Response:** `[]` (lista vazia).

---

### TC-011 — POST /v1/routing-rules
- **Status esperado:** 201  
- **Status retornado:** 201 ✅
- **Request:**
  ```json
  { "priority": 1, "conditionType": "DOCUMENT_TYPE", "conditionValue": "CONTRACT", "provider": "CLICKSIGN", "active": true }
  ```
- **Response:**
  ```json
  { "id": 1, "userId": "admin", "priority": 1, "conditionType": "DOCUMENT_TYPE", "conditionValue": "CONTRACT", "provider": "CLICKSIGN", "active": true, "createdAt": "2026-05-15T15:29:12.788286" }
  ```

---

### TC-012 — PUT /v1/routing-rules/1
- **Status esperado:** 200  
- **Status retornado:** 200 ✅
- **Request:** `{ "priority": 2, "active": false, ... }`
- **Response:** Registro atualizado com `priority: 2` e `active: false`.

---

### TC-013 — DELETE /v1/routing-rules/1
- **Status esperado:** 204  
- **Status retornado:** 204 ✅
- **Response:** Body vazio.

---

### TC-014 — POST /v1/users/change-password (senha inválida)
- **Status esperado:** 400 (violação de policy)  
- **Status retornado:** 400 ✅
- **Request:** `{ "currentPassword": "admin123", "newPassword": "admin123" }`
- **Response:** Erro de validação — senha deve ter ≥12 caracteres, maiúsculas, minúsculas, números e especiais.

---

### TC-015 — POST /v1/users/change-password (senha válida)
- **Status esperado:** 200  
- **Status retornado:** 200 ✅
- **Request:** `{ "currentPassword": "admin123", "newPassword": "Admin@12345678" }`
- **Response:** Body vazio.

---

### TC-016 — POST /v1/whatsapp/acceptance (sem autenticação)
- **Status esperado:** (público conforme doc)  
- **Status retornado:** 401
- **Resultado:** ⚠️ DESCOBERTA — endpoint **requer Bearer token**, contrariando a documentação inicial. O endpoint está protegido pelo Spring Security mesmo não tendo `@PreAuthorize` explícito.

---

### TC-017 — POST /v1/whatsapp/acceptance (campo em camelCase — body errado)
- **Status esperado:** 400  
- **Status retornado:** 400 ✅
- **Observação:** Campos usam `@JsonProperty` com snake_case (`signer_phone`, `signer_name`, `sender_name_option`). camelCase é rejeitado com erro de validação.

---

### TC-018 — POST /v1/whatsapp/acceptance (snake_case correto)
- **Status esperado:** 200  
- **Status retornado:** 200 ✅
- **Request:**
  ```json
  {
    "title": "Teste QA",
    "sender_name_option": "user_name",
    "sender_phone": "5511999999999",
    "message": "Por favor aceite este documento",
    "signer_phone": "5511988888888",
    "signer_name": "Signatario QA"
  }
  ```
- **Response:**
  ```json
  {
    "externalId": "bf74739a-972d-42a2-99fe-291c6504908b",
    "title": "Teste QA",
    "signerPhone": "5511988888888",
    "status": "enqueued",
    "statusDescription": "Aceite criado e aguardando envio via WhatsApp.",
    "createdAt": "2026-05-15T15:30:36"
  }
  ```

---

### TC-019 — GET /v1/admin/users como OPERATOR (autorização)
- **Status esperado:** 403  
- **Status retornado:** 403 ✅
- **Observação:** Usuário `qa.test.user` (OPERATOR) bloqueado corretamente de endpoint ADMIN.

---

### TC-020 — POST /v1/admin/users com role=ADMIN (bloqueio de escalada)
- **Status esperado:** 400  
- **Status retornado:** 400 ✅
- **Request:** `{ "role": "ADMIN", ... }`
- **Response:** `"Role inválida. Valores aceitos: VIEWER, OPERATOR, WEBHOOK"` — ADMIN não pode ser atribuído via API.

---

### TC-021 — GET /v1/signatures/{id} com ID inexistente
- **Status esperado:** 404  
- **Status retornado:** 404 ✅
- **Response:** `"Provider não informado e envelope não encontrado localmente para o ID: nonexistent-id-abc123"`

---

## Cenários de Erro

### TC-022 — Login com senha errada
- **Status esperado:** 401  
- **Status retornado:** 401 ✅
- **Request:** `{ "username": "admin", "password": "wrongpassword" }`
- **Response:** `"Usuário ou senha incorretos."`

---

### TC-023 — MFA com código inválido
- **Status esperado:** 400 ou 401  
- **Status retornado:** 401 ✅
- **Request:** `{ "mfaToken": "...", "code": "000000" }`
- **Response:** `"Código inválido ou expirado"`

---

### TC-024 — Acesso sem token
- **Status esperado:** 401  
- **Status retornado:** 401 ✅
- **Response:** `"Você não tem permissão para acessar este recurso ou seu token expirou."`

---

### TC-025 — Acesso com token inválido
- **Status esperado:** 401  
- **Status retornado:** 401 ✅
- **Request:** Header `Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.invalid.token`
- **Response:** `"Você não tem permissão para acessar este recurso ou seu token expirou."`

---

### TC-026 — Rate limit: 6 tentativas de login consecutivas
- **Status esperado:** 429 na 6ª tentativa  
- **Status retornado:** 429 na tentativa 6 ✅

| Tentativa | HTTP Status |
|-----------|-------------|
| 1 | 401 |
| 2 | 401 |
| 3 | 401 |
| 4 | 401 |
| 5 | 401 |
| 6 | 429 ✅ |

---

## Resumo Executivo

| # | Cenário | Esperado | Retornado | Resultado |
|---|---------|----------|-----------|-----------|
| TC-001 | Login válido | 200 | 200 | ✅ Passou |
| TC-002 | Consulta MFA no banco | código obtido | `817423` | ✅ Passou |
| TC-003 | MFA verify (código válido) | 200 | 200 | ✅ Passou |
| TC-004 | Refresh token | 200 | 200 | ✅ Passou |
| TC-005 | Logout | 204 | 204 | ✅ Passou |
| TC-006 | GET /admin/users (ADMIN) | 200 | 200 | ✅ Passou |
| TC-007 | POST /admin/users | 201 | 201 | ✅ Passou |
| TC-008 | GET /signatures | 200 | 200 | ✅ Passou |
| TC-009 | GET /signatures paginado | 200 | 200 | ✅ Passou |
| TC-010 | GET /routing-rules | 200 | 200 | ✅ Passou |
| TC-011 | POST /routing-rules | 201 | 201 | ✅ Passou |
| TC-012 | PUT /routing-rules/{id} | 200 | 200 | ✅ Passou |
| TC-013 | DELETE /routing-rules/{id} | 204 | 204 | ✅ Passou |
| TC-014 | change-password senha fraca | 400 | 400 | ✅ Passou |
| TC-015 | change-password senha válida | 200 | 200 | ✅ Passou |
| TC-016 | WhatsApp sem auth | público | 401 | ⚠️ Divergência de doc |
| TC-017 | WhatsApp body camelCase | 400 | 400 | ✅ Passou |
| TC-018 | WhatsApp body snake_case | 200 | 200 | ✅ Passou |
| TC-019 | Admin como OPERATOR | 403 | 403 | ✅ Passou |
| TC-020 | Criar user com role ADMIN | 400 | 400 | ✅ Passou |
| TC-021 | GET /signatures ID inválido | 404 | 404 | ✅ Passou |
| TC-022 | Login senha errada | 401 | 401 | ✅ Passou |
| TC-023 | MFA código inválido | 400/401 | 401 | ✅ Passou |
| TC-024 | Acesso sem token | 401 | 401 | ✅ Passou |
| TC-025 | Acesso token inválido | 401 | 401 | ✅ Passou |
| TC-026 | Rate limit 6 tentativas | 429 na 6ª | 429 | ✅ Passou |

**Total: 26 cenários testados — 25 ✅ Passou / 0 ❌ Falhou / 1 ⚠️ Divergência de documentação**

---

## Observações e Descobertas

1. **`/v1/whatsapp/acceptance` requer autenticação:** A controller não possui `@PreAuthorize` explícito, mas o Spring Security global protege todos os endpoints não listados em `permitAll()`. Documentar que este endpoint exige Bearer token.

2. **`CreateWhatsAppAcceptanceCommand` usa snake_case via `@JsonProperty`:** Contraria o padrão camelCase do resto da API. Avaliar padronização ou documentar explicitamente no Swagger.

3. **E-mails criptografados no GET /admin/users:** Retorna dados criptografados AES/GCM na resposta (`Am3n66kR5P7g7+ue:...`). Confirmar se o comportamento é intencional ou se deveria haver descriptografia para admin.

4. **Rate limit ativo a partir da 6ª tentativa:** O Resilience4j está corretamente configurado — primeiros 5 retornam 401, 6ª retorna 429. Comportamento consistente.

5. **Política de senha:** Mínimo de 12 caracteres, maiúsculas, minúsculas, números e caracteres especiais. Validação funcionando corretamente.

6. **Isolamento de roles:** Escalada de privilégio via API bloqueada — role ADMIN não pode ser atribuída, e endpoints admin retornam 403 para OPERATOR.
