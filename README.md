# 📄 SignFlow

## 🚀 Sobre o projeto

O **SignFlow** é uma API desenvolvida em **Spring Boot** para integração com a plataforma de assinaturas digitais Clicksign, permitindo gerenciar fluxos de assinatura eletrônica de forma automatizada.

A aplicação expõe endpoints REST documentados via Swagger para facilitar integração com outros sistemas.

---

## 🧱 Tecnologias utilizadas

* Java 17
* Spring Boot 3.3.4
* Spring Web
* Springdoc OpenAPI (Swagger)
* Maven
* Integração com API externa (Clicksign)

---

## ⚙️ Configurações principais

### 🔗 Integração com Clicksign

```yaml
clicksign:
  api:
    url: https://sandbox.clicksign.com/api/v3
    token: ${CLICKSIGN_API_TOKEN}
```

⚠️ **Importante:**
Nunca exponha seu token em repositórios públicos. Utilize variáveis de ambiente.

---

## 📄 Documentação da API (Swagger)

Após subir a aplicação, acesse:

* Swagger UI:
  http://localhost:8080/swagger-ui.html

* OpenAPI JSON:
  http://localhost:8080/api-docs

---

## ▶️ Como executar o projeto

### 🔹 Executar via JAR

```bash
java -jar SignFlow-0.0.1-SNAPSHOT.jar
```

---

### 🔹 Executar via Maven

```bash
mvn spring-boot:run
```

---

## 🌐 Variáveis de ambiente

Exemplo:

```bash
export CLICKSIGN_API_TOKEN=seu_token_aqui
export SWAGGER_SERVER_URL=https://sua-url.com
```

---

## 📦 Estrutura do projeto

```
com.signflow
 ├── controller        # Endpoints REST
 ├── service           # Regras de negócio
 ├── client            # Integração com Clicksign
 ├── dto               # Objetos de transferência
 ├── config            # Configurações
 └── SignFlowApplication
```

---

## 🔐 Segurança

* Utilize variáveis de ambiente para credenciais
* Nunca versionar tokens no código
* Recomenda-se uso de Vault ou Secrets Manager em produção

---

## 📌 Funcionalidades

* Criação de documentos para assinatura
* Envio de documentos para signatários
* Consulta de status de assinatura
* Gestão de fluxo de assinaturas

---

## 📄 Deploy

A aplicação pode ser executada em:

* Docker
* Kubernetes / OpenShift
* Execução standalone via JAR

---

## 🧪 Ambiente de testes

API Sandbox:

```
https://sandbox.clicksign.com/api/v3
```

---

## 🐳 Exemplo Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/SignFlow-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

## 🐳 Exemplo docker-compose

```yaml
version: '3.8'

services:
  signflow:
    build: .
    ports:
      - "8080:8080"
    environment:
      - CLICKSIGN_API_TOKEN=seu_token_aqui
```

---

## 🧪 Testes

Sugestão de stack:

* JUnit
* Mockito
* Spring Boot Test

---

## 📈 Melhorias futuras

* Autenticação (JWT / OAuth2)
* Cache com Redis
* Mensageria (Kafka / RabbitMQ)
* Observabilidade (Grafana + Prometheus)
* Testes automatizados completos

---

## 👨‍💻 Autor

Bernardo Trindade

---

## ⚠️ Observações

* Revise configurações sensíveis antes de subir para produção
* Utilize profiles (`dev`, `hml`, `prod`) para separar ambientes
* Garanta logs estruturados para monitoramento

---
