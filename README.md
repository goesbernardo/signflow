# SignFlow

O **SignFlow** é uma API de gerenciamento de assinaturas eletrônicas, projetada com foco em resiliência, segurança e extensibilidade. Atualmente, oferece integração completa com o provedor **ClickSign**.

## 🚀 Tecnologias

- **Java 17** e **Spring Boot 3.3.4**
- **Spring Security** com **JWT (JSON Web Token)**
- **Spring Data JPA** com **PostgreSQL**
- **OpenFeign** para integrações de API
- **Resilience4j** (Circuit Breaker) para tolerância a falhas
- **SpringDoc OpenAPI** (Swagger) para documentação
- **Docker** e **Docker Compose**

## 🏗️ Arquitetura

O projeto segue princípios de **Arquitetura Hexagonal (Clean Architecture)**, separando as regras de negócio das implementações técnicas (gateways, adaptadores de persistência e controladores).

## 🔐 Segurança

A API está protegida por autenticação JWT.

### Usuário Padrão (Criado na inicialização)
- **Usuário:** `admin`
- **Senha:** `admin123`

### Como se autenticar
1. Realize o login no endpoint `/api/v1/auth/login`.
2. Utilize o token retornado no cabeçalho `Authorization` de todas as requisições subsequentes:
   ```http
   Authorization: Bearer <seu_token_jwt>
   ```

## ⚙️ Configuração e Execução

### Pré-requisitos
- Docker e Docker Compose
- JDK 17
- Maven (ou utilize o `./mvnw` incluso)

### Variáveis de Ambiente
As seguintes variáveis podem ser configuradas no `application.yml` ou passadas via sistema:

| Variável | Descrição | Valor Padrão |
|----------|-----------|--------------|
| `CLICKSIGN_URL` | URL da API da ClickSign | `https://sandbox.clicksign.com/api/v3` |
| `CLICKSIGN_API_TOKEN` | Token de acesso da ClickSign | (Token de Sandbox configurado) |
| `JWT_SECRET` | Chave secreta para geração do JWT | (Chave fixa para desenvolvimento) |

### Execução com Docker
O projeto inclui um `docker-compose.yaml` para subir o banco de dados PostgreSQL:

```bash
docker-compose up -d
```

### Execução da Aplicação
```bash
./mvnw spring-boot:run
```

## 📖 Documentação da API (Swagger)

A documentação interativa da API está disponível em:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Docs:** `http://localhost:8080/api-docs`

## 🛠️ Endpoints Principais

### Autenticação
- `POST /api/v1/auth/login`: Realiza login e retorna o token JWT.

### Assinaturas (Signature)
Todos os endpoints abaixo exigem o header `provider` (ex: `CLICKSIGN`) e o token `Bearer`.

- `POST /api/v1/signatures`: Cria um novo envelope.
- `GET /api/v1/signatures/{externalId}`: Busca detalhes de um envelope.
- `PATCH /api/v1/signatures/{externalId}`: Edita um envelope.
- `POST /api/v1/signatures/{externalId}/signers`: Adiciona um signatário.
- `POST /api/v1/signatures/{externalId}/documents`: Adiciona um documento.
- `POST /api/v1/signatures/{externalId}/requirements`: Vincula signatário a um documento.
- `PATCH /api/v1/signatures/{externalId}/activate`: Ativa o envelope para assinatura.

## 🛡️ Resiliência
A integração com provedores externos é protegida por um **Circuit Breaker** (Resilience4j). Caso o provedor (ex: ClickSign) esteja instável, a aplicação aciona métodos de fallback para evitar falhas em cascata, retornando mensagens amigáveis de erro de integração.

## 🗄️ Persistência e Auditoria
A aplicação mantém um registro local de:
- **Envelopes:** Status, ID externo e usuário criador.
- **Signatários e Documentos:** Vinculados aos envelopes.
- **Trilha de Auditoria:** Eventos imutáveis que registram cada mudança de status do envelope (Ex: `PROCESSING` -> `SUCCESS`).
