# SignFlow

O **SignFlow** é uma API de gerenciamento de assinaturas eletrônicas, projetada com foco em resiliência, segurança e extensibilidade. O sistema é **agnóstico a provedores**, permitindo a integração com diversos serviços de assinatura através de uma interface unificada.



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
As variáveis de configuração são organizadas por provedor. Exemplo de configuração para a ClickSign e JWT:

| Variável | Descrição | Valor Padrão |
|----------|-----------|--------------|
| `JWT_SECRET` | Chave secreta para geração do JWT | (Obrigatório via ENV) |
| `ADMIN_USERNAME` | Username do administrador padrão (Migration) | `admin` |
| `ADMIN_PASSWORD` | Senha BCrypt do administrador padrão (Migration) | `admin123` (Criptografado) |
| `CLICKSIGN_URL` | URL da API da ClickSign | `https://sandbox.clicksign.com/api/v3` |
| `CLICKSIGN_API_TOKEN` | Token de acesso da ClickSign | (Token de Sandbox) |

*Nota: Para novos provedores, siga o padrão `NOMEPROVEDOR_URL` e `NOMEPROVEDOR_TOKEN`.*

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
A API utiliza o header `provider` para rotear as chamadas para o adaptador correspondente (ex: `CLICKSIGN`). Todos os endpoints abaixo exigem esse header e o token `Bearer`.

- `POST /api/v1/signatures`: Cria um novo envelope.
- `GET /api/v1/signatures/{externalId}`: Busca detalhes de um envelope.
- `PATCH /api/v1/signatures/{externalId}`: Edita um envelope.
- `POST /api/v1/signatures/{externalId}/signers`: Adiciona um signatário.
- `POST /api/v1/signatures/{externalId}/documents`: Adiciona um documento.
- `POST /api/v1/signatures/{externalId}/requirements`: Vincula signatário a um documento.
- `PATCH /api/v1/signatures/{externalId}/activate`: Ativa o envelope para assinatura.
- `GET /api/v1/signatures/{externalId}/timeline`: Retorna a trilha de auditoria (eventos) do envelope.

## 🌍 Internacionalização (i18n)

A API suporta múltiplos idiomas para as mensagens de erro e respostas do sistema. O idioma é resolvido com base no cabeçalho `Accept-Language` da requisição.

- **Idiomas suportados:**
  - Português (Brasil): `pt-BR` (Padrão)
  - Inglês: `en-US` ou `en`
  - Espanhol: `es-ES` ou `es`

Exemplo de uso:
```http
Accept-Language: en-US
```

## 🛡️ Resiliência e Erros
A integração com provedores externos é protegida por um **Circuit Breaker** (Resilience4j). Caso um provedor esteja instável, a aplicação aciona métodos de fallback que retornam mensagens de erro internacionalizadas. Cada provedor possui sua própria configuração de circuit breaker, garantindo isolamento entre falhas.

## 🗄️ Persistência e Auditoria
A aplicação mantém um registro local de:
- **Envelopes:** Status, ID externo e usuário criador.
- **Signatários e Documentos:** Vinculados aos envelopes.
- **Trilha de Auditoria:** Eventos imutáveis que registram cada mudança de status do envelope (Ex: `PROCESSING` -> `SUCCESS`).
