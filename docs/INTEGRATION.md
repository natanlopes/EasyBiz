# EasyBiz - API Integration Guide

**Version:** 1.1.0
**Last Updated:** 2026-02-15
**Backend:** Spring Boot 3.4 - Java 17 - PostgreSQL
**Frontend:** Kotlin Multiplatform - Compose Multiplatform
**Audience:** Frontend Engineers (Edson Neto)
**Maintainer:** Natanael Lopes (Backend)

---

## Table of Contents

1. [Quick Start](#1-quick-start)
2. [Environments](#2-environments)
3. [Authentication](#3-authentication)
4. [API Reference](#4-api-reference)
5. [WebSocket - Real-Time Chat](#5-websocket---real-time-chat)
6. [Error Handling](#6-error-handling)
7. [Screen-to-Endpoint Mapping](#7-screen-to-endpoint-mapping)
8. [Kotlin/Ktor Implementation Guide](#8-kotlinktor-implementation-guide)
9. [Data Models (DTOs)](#9-data-models-dtos)
10. [Status Codes and Business Rules](#10-status-codes-and-business-rules)
11. [Testing Checklist](#11-testing-checklist)
12. [Changelog](#12-changelog)

---

## 1. Quick Start

```bash
# 1. Clone backend
git clone https://github.com/natanlopes/EasyBiz.git

# 2. Start PostgreSQL
docker-compose up -d

# 3. Run backend
./mvnw spring-boot:run

# 4. Verify
curl http://localhost:8080/swagger-ui/index.html
```

**First API call - Register + Login:**

```
bash
# Register
curl -X POST http://localhost:8080/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nomeCompleto":"Edson Neto","email":"edson@test.com","senha":"123456"}'

# Login -> get token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"edson@test.com","senha":"123456"}'

# Response: { "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

---

## 2. Environments

| Environment | Base URL                                 | Status  |
|-------------|------------------------------------------|---------|
| Local       | `http://localhost:8080`                  | Active  |
| Staging     | `https://easybiz-staging.up.railway.app` | Planned |
| Production  | `https://api.easybiz.com.br`             | Planned |

**Important:** All endpoints require HTTPS in staging/production. Local development uses HTTP.

---

## 3. Authentication

EasyBiz uses **JWT (JSON Web Token)** with stateless authentication. No sessions, no cookies.

### 3.1 Auth Flow

```
[App]                          [API]                    [Database]
  |                              |                           |
  | POST /auth/login             |                           |
  |----------------------------->|                           |
  |                              | Validate BCrypt           |
  |                              |-------------------------->|
  |                              |<--------------------------|
  | { token: "eyJ.." }          |                           |
  |<-----------------------------|                           |
  |                              |                           |
  | GET /usuarios/me             |                           |
  | Authorization: Bearer eyJ... |                           |
  |----------------------------->|                           |
  |                              | Extract email from token  |
  | { id, nome, ... }           |                           |
  |<-----------------------------|                           |
```

### 3.2 Token Rules

| Property      | Value                                |
|---------------|--------------------------------------|
| Algorithm     | HS256                                |
| Expiration    | 24 hours (86400000 ms)               |
| Header format | `Authorization: Bearer <token>`      |
| Token content | User email embedded in subject claim |

### 3.3 How to Use the Token

Every request (except public endpoints) **must** include the token:

```
GET /usuarios/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

### 3.4 Token Expiration Handling

When the token expires, the API returns `401 Unauthorized`. The app should:

1. Detect `401` response
2. Redirect user to login screen
3. Clear stored token
4. After re-login, retry the failed request

> **Note (V1):** There is no refresh token mechanism. The user must re-login after 24h.

### 3.5 Password Recovery Flow

```
[App]                          [API]                    [Email]
  |                              |                         |
  | POST /auth/esqueci-senha     |                         |
  | { email }                    |                         |
  |----------------------------->|                         |
  |                              | Generate 6-digit code   |
  |                              |------------------------>|
  |                              |  Send code via email    |
  | { mensagem: "Se o email..." }|                         |
  |<-----------------------------|                         |
  |                              |                         |
  | POST /auth/redefinir-senha   |                         |
  | { token: "482917",           |                         |
  |   novaSenha: "nova123" }     |                         |
  |----------------------------->|                         |
  |                              | Validate code           |
  |                              | Update password (BCrypt)|
  | { mensagem: "Senha..." }     |                         |
  |<-----------------------------|                         |
```

**Security notes:**
- Code expires in 15 minutes
- Code is single-use (marked as used after successful reset)
- API always returns 200 on esqueci-senha (doesn't reveal if email exists)
- Previous unused codes are invalidated when a new one is requested

---

## 4. API Reference

### Base Headers (all requests)

```
Content-Type: application/json
Authorization: Bearer <token>    # except public endpoints
```

---

### 4.1 Auth

#### `POST /usuarios` — Register User

**Auth required:** No

**Request:**
```json
{
  "email": "edson@email.com",
  "senha": "minhasenha123"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Error `401 Unauthorized`:**
```json
{
  "timestamp": "2026-02-15T10:00:00",
  "status": 401,
  "error": "Nao Autenticado",
  "message": "Credenciais invalidas"
}
```

> **Security:** Password is validated with BCrypt. The API never reveals whether the email exists or the password is wrong - always returns "Credenciais invalidas" for both cases.

---

#### `POST /auth/esqueci-senha` - Request Password Recovery

**Auth required:** No

**Request:**
```json
{
  "email": "edson@email.com"
}
```

**Response `200 OK`:**
```json
{
  "mensagem": "Se o email estiver cadastrado, enviaremos um codigo de recuperacao."
}
```

**Validation:**
- `email`: required, valid email format

---

#### `POST /auth/redefinir-senha` - Reset Password

**Auth required:** No

**Request:**
```json
{
  "token": "482917",
  "novaSenha": "minhaNovaSenha123"
}
```

**Response `200 OK`:**
```json
{
  "mensagem": "Senha redefinida com sucesso."
}
```

**Error `400 Bad Request`:**
```json
{
  "timestamp": "2026-02-15T10:00:00",
  "status": 400,
  "error": "Erro de Regra de Negocio",
  "message": "Codigo invalido ou expirado"
}
```

**Validation:**
- `token`: required (6-digit code received by email)
- `novaSenha`: required, min 6 characters

---

### 4.2 Users

#### `POST /usuarios` - Register User

**Auth required:** No

**Request:**
```json
{
  "nomeCompleto": "Edson Neto",
  "email": "edson@email.com",
  "senha": "minhasenha123"
}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "nome": "Edson Neto",
  "email": "edson@email.com",
  "fotoUrl": null
}
```

**Error `400 Bad Request`:**
```json
{
  "timestamp": "2026-02-15T10:00:00",
  "status": 400,
  "error": "Erro de Regra de Negocio",
  "message": "Email ja cadastrado"
}
```

**Validation rules:**
- `nomeCompleto`: required
- `email`: required, valid email format, unique
- `senha`: required

---

#### `GET /usuarios/me` - Get Current User Profile

**Auth required:** Yes

**Response `200 OK`:**
```json
{
  "id": 1,
  "nome": "Edson Neto",
  "email": "edson@email.com",
  "fotoUrl": "https://example.com/foto.jpg"
}
```

> **Frontend note:** Call this endpoint immediately after login to load the user profile, avatar, and permissions. Store the response in local state.

---

#### `GET /usuarios/{id}` - Get Public User Profile

**Auth required:** Yes

**Response `200 OK`:** Same structure as `/usuarios/me`

**Error `404 Not Found`:** User not found

---

#### `PATCH /usuarios/me/foto` - Update Profile Photo

**Auth required:** Yes

**Request:**
```json
{
  "url": "https://example.com/nova-foto.jpg"
}
```

**Response:** `204 No Content`

**Validation:**
- `url`: required, valid URL format

---

### 4.3 Businesses (Negocios)

#### `POST /negocios` - Create Business

**Auth required:** Yes (owner becomes the authenticated user)

**Request:**
```json
{
  "nome": "Barbearia do Edson",
  "categoria": "BARBEARIA"
}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "nome": "Barbearia do Edson",
  "categoria": "BARBEARIA",
  "usuarioId": 1,
  "nomeUsuario": "Edson Neto",
  "ativo": true,
  "latitude": null,
  "longitude": null,
  "enderecoCompleto": null,
  "notaMedia": 0.0,
  "logoUrl": null
}
```

**Available categories (enum `TipoNegocio`):**
```
BARBEARIA, MECANICA, ELETRICISTA, ENCANADOR, PEDREIRO,
PINTOR, PERSONAL_TRAINER, MOTOTAXI, FRETE, LIMPEZA,
JARDINAGEM, COZINHEIRO, COSTUREIRA, MANICURE, MASSAGISTA,
FOTOGRAFO, PROFESSOR_PARTICULAR, VETERINARIO, OUTROS
```

---

#### `GET /negocios/busca` - Smart Search

**Auth required:** No

**Parameters:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `lat` | double | Yes | User's current latitude |
| `lon` | double | Yes | User's current longitude |
| `busca` | string | No | Search term (name or category) |

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "nome": "Barbearia do Edson",
    "categoria": "BARBEARIA",
    "usuarioId": 1,
    "nomeUsuario": "Edson Neto",
    "ativo": true,
    "latitude": -23.5505,
    "longitude": -46.6333,
    "enderecoCompleto": "Rua das Flores, 123",
    "notaMedia": 4.5,
    "logoUrl": "https://example.com/logo.jpg"
  }
]
```

> **How it works:** Uses Haversine formula in PostgreSQL to calculate distance. Default radius is 30km. Results are sorted by rating. Businesses without coordinates are excluded.

> **Frontend note:** Request location permission from the user, then pass their GPS coordinates to this endpoint.

---

#### `PATCH /negocios/{id}/logo` - Update Business Logo

**Auth required:** Yes (must be the business owner)

**Request:**
```json
{
  "url": "https://example.com/new-logo.jpg"
}
```

**Response:** `204 No Content`

**Error `403 Forbidden`:** If authenticated user is not the owner

---

### 4.4 Orders (Pedidos)

#### `POST /pedidos` - Create Order (Client -> Business)

**Auth required:** Yes (authenticated user becomes the client)

**Request:**
```json
{
  "negocioId": 1,
  "descricao": "Corte de cabelo degradê + barba"
}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "clienteId": 2,
  "clienteNome": "Joao Silva",
  "negocioId": 1,
  "negocioNome": "Barbearia do Edson",
  "descricao": "Corte de cabelo degradê + barba",
  "dataDesejada": null,
  "status": "ABERTO",
  "criadoEm": "2026-02-15T14:30:00"
}
```

> **Business rule:** A new order creates a unique "negotiation room" (chat channel). Each order has its own independent chat history.

---

#### `GET /pedidos` - List My Orders (Paginated)

**Auth required:** Yes

**Parameters:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | 20 | Items per page |
| `sort` | string | - | Sort field (e.g. `criadoEm,desc`) |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": 1,
      "clienteId": 2,
      "clienteNome": "Joao Silva",
      "negocioId": 1,
      "negocioNome": "Barbearia do Edson",
      "descricao": "Corte degradê + barba",
      "dataDesejada": null,
      "status": "ACEITO",
      "criadoEm": "2026-02-15T14:30:00"
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

> **Note:** Returns all orders where the user is either the client OR the business owner.

---

#### `PATCH /pedidos/{id}/aceitar` - Accept Order

**Auth required:** Yes (must be the business owner)

**Response:** `204 No Content`

**Error `403 Forbidden`:** Not the business owner
**Error `400 Bad Request`:** Order is not in `ABERTO` status

---

#### `PATCH /pedidos/{id}/recusar` - Decline Order

**Auth required:** Yes (must be the business owner)

**Response:** `204 No Content`

---

#### `PATCH /pedidos/{id}/concluir` - Complete Order

**Auth required:** Yes (must be the business owner)

**Response:** `204 No Content`

**Error `400 Bad Request`:** Order is not in `ACEITO` status

> **Business rule:** Cannot complete an order that was not accepted first. The flow is strictly: ABERTO -> ACEITO -> CONCLUIDO.

---

#### `PATCH /pedidos/{id}/cancelar` - Cancel Order

**Auth required:** Yes (client or provider)

**Response:** `204 No Content`

---

### 4.5 Reviews (Avaliacoes)

#### `POST /avaliacoes/pedido/{pedidoId}` - Create Review

**Auth required:** Yes (must be the client of the order)

**Request:**
```json
{
  "nota": 5,
  "comentario": "Excelente serviço, recomendo!"
}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "nota": 5,
  "comentario": "Excelente serviço, recomendo!",
  "dataAvaliacao": "2026-02-15T16:00:00",
  "pedidoId": 1,
  "avaliadorNome": "Joao Silva",
  "avaliadoNome": "Edson Neto",
  "negocioId": 1,
  "negocioNome": "Barbearia do Edson"
}
```

**Validation rules:**
- `nota`: required, integer 1-5
- `comentario`: optional, max 500 chars
- `pedidoId`: must be a `CONCLUIDO` order
- Only one review per order
- Only the client can review

> **Side effect:** After creating a review, the business's `notaMedia` and `totalAvaliacoes` are automatically recalculated.

---

### 4.6 Chat (Messages)

#### `GET /pedidos/{pedidoId}/mensagens` - Load Chat History

**Auth required:** Yes (must be client or business owner of the order)

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "pedidoServicoId": 1,
    "remetenteId": 2,
    "remetenteNome": "Joao Silva",
    "conteudo": "Ola, gostaria de agendar para sabado",
    "enviadoEm": "2026-02-15T14:35:00",
    "lida": true,
    "lidaEm": "2026-02-15T14:36:00",
    "remetenteFotoUrl": "https://example.com/foto.jpg"
  }
]
```

**Error `403 Forbidden`:** User is not a participant of this order (IDOR protection)

> **Important:** Always load history via REST first when entering the chat screen, then connect WebSocket for real-time messages.

---

#### `POST /pedidos/{pedidoId}/mensagens` - Send Message via REST

**Auth required:** Yes

**Request:**
```json
{
  "conteudo": "Ola, gostaria de agendar!"
}
```

**Response `200 OK`:** Same structure as chat history item

---

#### `POST /pedidos/{pedidoId}/mensagens/lidas` - Mark Messages as Read

**Auth required:** Yes

**Response:** `204 No Content`

---

## 5. WebSocket - Real-Time Chat

### 5.1 Architecture

```
[App (Client)]     STOMP/SockJS     [Backend (Broker)]
      |           /ws-chat                |
      |                                   |
      | SUBSCRIBE                         |
      | /topic/mensagens/{pedidoId}       |
      |---------------------------------->|
      |                                   |
      | SEND                              |
      | /app/chat/{pedidoId}              |
      | { conteudo }                      |
      |---------------------------------->|
      |                                   | 1. Validate JWT
      |                                   | 2. Extract userId
      |                                   | 3. Save to DB
      | MESSAGE                           | 4. Broadcast
      | /topic/mensagens/{pedidoId}       |
      | { id, conteudo, remetenteId }     |
      |<----------------------------------|
```

### 5.2 Connection

| Property | Value                                                                           |
|----------|---------------------------------------------------------------------------------|
| Endpoint | `ws://localhost:8080/ws-chat` (dev) / `wss://api.easybiz.com.br/ws-chat` (prod) |
| Protocol | STOMP over SockJS                                                               |
| Auth     | `Authorization: Bearer <token>` header during CONNECT                           |

### 5.3 Connection Flow

```
1. CONNECT with JWT header
2. SUBSCRIBE to /topic/mensagens/{pedidoId}
3. SEND messages to /app/chat/{pedidoId}
4. RECEIVE messages from subscription
5. DISCONNECT when leaving chat screen
```

### 5.4 STOMP Frames

**Connect:**
```
CONNECT
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
accept-version:1.1
heart-beat:10000,10000

\0
```

**Subscribe to chat room:**
```
SUBSCRIBE
id:sub-0
destination:/topic/mensagens/1

\0
```

**Send message:**
```
SEND
destination:/app/chat/1
content-type:application/json

{"conteudo":"Ola!"}
\0
```

**Receive message (from server):**
```
MESSAGE
destination:/topic/mensagens/1
content-type:application/json

{"id":45,"pedidoServicoId":1,"remetenteId":2,"remetenteNome":"Joao","conteudo":"Ola!","enviadoEm":"2026-02-15T14:35:00","lida":false,"lidaEm":null,"remetenteFotoUrl":null}
```

### 5.5 Additional Real-Time Features

**Typing indicator:**
```
SEND
destination:/app/chat/1/digitando
content-type:application/json

{"usuarioId":7,"usuarioNome":"Cliente","digitando":true}
```

Subscribe: `/topic/mensagens/{pedidoId}/digitando`

**Read confirmation:**
```
SEND
destination:/app/chat/1/lida/45
```

Subscribe: `/topic/mensagens/{pedidoId}/lida`
Also emits to: `/topic/mensagens/{pedidoId}/ultimo-visto`

### 5.6 Security

- JWT is validated during the WebSocket handshake (CONNECT frame)
- If the token is invalid or expired, the connection is rejected
- The sender ID (`remetenteId`) is ALWAYS extracted from the token - never from the request body
- This prevents spoofing (a user cannot send messages as another user)

---

## 6. Error Handling

### 6.1 Standard Error Response

All errors follow this format:

```json
{
  "timestamp": "2026-02-15T10:00:00",
  "status": 400,
  "error": "Erro de Regra de Negocio",
  "message": "Mensagem legivel em portugues"
}
```

### 6.2 HTTP Status Codes

| Code  | Meaning           | When                                               |
|-------|-------------------|----------------------------------------------------|
| `200` | OK                | Successful GET, POST, PATCH                        |
| `204` | No Content        | Successful operation with no response body         |
| `400` | Bad Request       | Validation error, business rule violation          |
| `401` | Unauthorized      | Missing/expired/invalid token                      |
| `403` | Forbidden         | Valid token but no permission (IDOR)               |
| `404` | Not Found         | Resource doesn't exist                             |
| `429` | Too Many Requests | Rate limit exceeded (10 req/min on login/register) |
| `500` | Internal Error    | Unexpected server error                            |

### 6.3 Common Error Messages

| Error                              | Cause                     | Frontend Action                  |
|------------------------------------|---------------------------|----------------------------------|
| `"Credenciais invalidas"`          | Wrong email/password      | Show error on login form         |
| `"Email ja cadastrado"`            | Duplicate email           | Show error on register form      |
| `"Negocio nao encontrado"`         | Invalid business ID       | Show "not found" screen          |
| `"Pedido nao encontrado"`          | Invalid order ID          | Navigate back                    |
| `"Pedido ja foi avaliado"`         | Duplicate review          | Disable review button            |
| `"Apenas o cliente pode cancelar"` | Provider tried to cancel  | Hide cancel button for providers |
| `"Pedido nao esta ABERTO"`         | Invalid status transition | Refresh order status             |
| `"Acesso negado"`                  | IDOR attempt              | Navigate back, show error toast  |

### 6.4 Recommended Error Handling in Kotlin

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(call())
    } catch (e: ClientRequestException) {
        when (e.response.status.value) {
            401 -> {
                TokenManager.clear()
                ApiResult.Error(401, "Sessao expirada")
            }
            403 -> ApiResult.Error(403, "Acesso negado")
            429 -> ApiResult.Error(429, "Muitas tentativas. Aguarde 1 minuto.")
            else -> {
                val body = e.response.bodyAsText()
                val msg = Json.decodeFromString<ApiErrorResponse>(body).message
                ApiResult.Error(e.response.status.value, msg)
            }
        }
    } catch (e: Exception) {
        ApiResult.Error(0, "Erro de conexao")
    }
}
```

---

## 7. Screen-to-Endpoint Mapping

### 7.1 Splash / App Init

```
App opens
  |-> Check if token exists in local storage
       |-> YES -> GET /usuarios/me
       |           |-> 200 -> Navigate to Home
       |           |-> 401 -> Navigate to Login
       |-> NO  -> Navigate to Login
```

### 7.2 Login Screen

```
User taps "Entrar"
  |-> POST /auth/login { email, senha }
       |-> 200 -> Store token -> GET /usuarios/me -> Navigate to Home
       |-> 401 -> Show "Credenciais invalidas"
       |-> 429 -> Show "Muitas tentativas. Aguarde 1 minuto."

User taps "Esqueci minha senha"
  |-> Navigate to Forgot Password Screen
```

### 7.3 Register Screen

```
User taps "Cadastrar"
  |-> POST /usuarios { nomeCompleto, email, senha }
       |-> 200 -> POST /auth/login (auto-login) -> Navigate to Home
       |-> 400 -> Show "Email ja cadastrado"
       |-> 429 -> Show "Muitas tentativas. Aguarde 1 minuto."
```

### 7.4 Forgot Password Screen

```
User enters email and taps "Enviar codigo"
  |-> POST /auth/esqueci-senha { email }
       |-> 200 -> Navigate to Reset Password Screen
       |-> Show message: "Se o email estiver cadastrado..."

User enters code + new password and taps "Redefinir"
  |-> POST /auth/redefinir-senha { token, novaSenha }
       |-> 200 -> Show success -> Navigate to Login
       |-> 400 -> Show "Codigo invalido ou expirado"
```

### 7.5 Home Screen (Search)

```
Screen loads
  |-> Request GPS permission
       |-> GET /negocios/busca?lat={lat}&lon={lon}
            |-> Render list of businesses sorted by rating

User types in search bar
  |-> GET /negocios/busca?busca={query}&lat={lat}&lon={lon}
```

### 7.6 Business Detail Screen

```
User taps a business card
  |-> Render business details from list data (no extra API call needed)

User taps "Solicitar Servico"
  |-> POST /pedidos { negocioId, descricao }
       |-> 200 -> Navigate to Chat Screen (with pedidoId)
       |-> 400 -> Show error
```

### 7.7 My Orders Screen

```
Screen loads
  |-> GET /pedidos?page=0&size=20
       |-> Render order cards grouped by status

Load more (infinite scroll)
  |-> GET /pedidos?page={nextPage}&size=20

Order card shows:
  - Business name
  - Description
  - Status badge (color-coded)
  - Created date

User taps an order
  |-> Navigate to Chat Screen (with pedidoId)
```

### 7.8 Chat Screen

```
Screen loads
  |-> GET /pedidos/{pedidoId}/mensagens  (load history)
  |-> CONNECT WebSocket /ws-chat (with JWT)
       |-> SUBSCRIBE /topic/mensagens/{pedidoId}

User sends message
  |-> SEND /app/chat/{pedidoId} { conteudo }

User typing
  |-> SEND /app/chat/{pedidoId}/digitando { usuarioId, usuarioNome, digitando }

Receive message (real-time)
  |-> MESSAGE from /topic/mensagens/{pedidoId}
       |-> Append to message list, scroll to bottom

Screen closes
  |-> DISCONNECT WebSocket
```

### 7.9 Provider Actions (Order Management)

```
Provider sees new order (status: ABERTO)
  |-> "Aceitar"  -> PATCH /pedidos/{id}/aceitar
  |-> "Recusar"  -> PATCH /pedidos/{id}/recusar

Provider finishes service (status: ACEITO)
  |-> "Concluir" -> PATCH /pedidos/{id}/concluir

Client sees order (status: ABERTO)
  |-> "Cancelar" -> PATCH /pedidos/{id}/cancelar

Client sees completed order (status: CONCLUIDO)
  |-> "Avaliar"  -> Navigate to Review Screen
```

### 7.10 Review Screen

```
User submits review
  |-> POST /avaliacoes/pedido/{pedidoId} { nota, comentario }
       |-> 200 -> Show success -> Navigate back
       |-> 400 -> Show "Pedido ja foi avaliado"
```

### 7.11 Profile / My Business Screen

```
Screen loads
  |-> GET /usuarios/me
       |-> Render user info

Update profile photo
  |-> PATCH /usuarios/me/foto { url }

Update business logo
  |-> PATCH /negocios/{id}/logo { url }
```

---

## 8. Kotlin/Ktor Implementation Guide

### 8.1 Ktor Client Setup (KMP)

```
kotlin
// core/networking/HttpClientFactory.kt

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(tokenProvider: () -> String?): HttpClient {
        return HttpClient(CIO) {

            // JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }

            // Logging (debug only)
            install(Logging) {
                level = LogLevel.BODY
            }

            // Default headers
            defaultRequest {
                contentType(ContentType.Application.Json)
                url("http://10.0.2.2:8080") // Android emulator -> localhost
                // url("http://localhost:8080") // iOS simulator
                // url("https://api.easybiz.com.br") // Production

                // Attach JWT token
                tokenProvider()?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }

            // Timeout
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
            }
        }
    }
}

```

> **Android Emulator:** Use `10.0.2.2` instead of `localhost` to reach the host machine.

### 8.2 Token Manager

```
kotlin
// core/networking/TokenManager.kt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object TokenManager {
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    fun save(token: String) {
        _token.value = token
        // TODO: Also persist to DataStore/SharedPreferences
    }

    fun get(): String? = _token.value

    fun clear() {
        _token.value = null
        // TODO: Also clear from DataStore/SharedPreferences
    }

    fun isLoggedIn(): Boolean = _token.value != null
}

```

### 8.3 API Service Example

```
kotlin
// features/auth/data/remote/AuthApi.kt

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class AuthApi(private val client: HttpClient) {

    suspend fun login(email: String, senha: String): TokenResponse {
        return client.post("/auth/login") {
            setBody(LoginRequest(email, senha))
        }.body()
    }

    suspend fun register(nome: String, email: String, senha: String): UserResponse {
        return client.post("/usuarios") {
            setBody(RegisterRequest(nome, email, senha))
        }.body()
    }

    suspend fun esqueciSenha(email: String): MessageResponse {
        return client.post("/auth/esqueci-senha") {
            setBody(EsqueciSenhaRequest(email))
        }.body()
    }

    suspend fun redefinirSenha(token: String, novaSenha: String): MessageResponse {
        return client.post("/auth/redefinir-senha") {
            setBody(RedefinirSenhaRequest(token, novaSenha))
        }.body()
    }

    suspend fun getMe(): UserResponse {
        return client.get("/usuarios/me").body()
    }
}

```

### 8.4 WebSocket Client (STOMP over SockJS)

```
kotlin
// core/networking/WebSocketManager.kt

// Recommended library for STOMP in KMP: Krossbow
// https://github.com/joffrey-bion/krossbow

class ChatWebSocketManager(
    private val tokenProvider: () -> String?
) {
    // Using krossbow:
    // val stompClient = StompClient(WebSocketClient.builtIn())
    //
    // suspend fun connect(pedidoId: Long) {
    //     val token = tokenProvider() ?: throw IllegalStateException("No token")
    //     val session = stompClient.connect(
    //         "ws://10.0.2.2:8080/ws-chat",
    //         customStompConnectHeaders = mapOf("Authorization" to "Bearer $token")
    //     )
    //
    //     // Subscribe to messages
    //     session.subscribe("/topic/mensagens/$pedidoId").collect { frame ->
    //         val message = Json.decodeFromString<MensagemResponse>(frame.bodyAsText)
    //         // Handle message
    //     }
    // }
    //
    // suspend fun sendMessage(pedidoId: Long, conteudo: String) {
    //     session.send("/app/chat/$pedidoId", """{"conteudo":"$conteudo"}""")
    // }
    //
    // suspend fun sendTyping(pedidoId: Long, userId: Long, userName: String) {
    //     session.send("/app/chat/$pedidoId/digitando",
    //         """{"usuarioId":$userId,"usuarioNome":"$userName","digitando":true}""")
    // }
    //
    // fun disconnect() {
    //     session.disconnect()
    // }
}
```

### 8.5 Koin DI Module

```
kotlin
// core/di/NetworkModule.kt

import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory.create { TokenManager.get() } }
    single { AuthApi(get()) }
    single { NegocioApi(get()) }
    single { PedidoApi(get()) }
    single { AvaliacaoApi(get()) }
    single { ChatWebSocketManager { TokenManager.get() } }
}

```

---

## 9. Data Models (DTOs)

### 9.1 Request Models

```kotlin
@Serializable
data class LoginRequest(val email: String, val senha: String)

@Serializable
data class RegisterRequest(val nomeCompleto: String, val email: String, val senha: String)

@Serializable
data class EsqueciSenhaRequest(val email: String)

@Serializable
data class RedefinirSenhaRequest(val token: String, val novaSenha: String)

@Serializable
data class CriarNegocioRequest(val nome: String, val categoria: String)

@Serializable
data class CriarPedidoRequest(val negocioId: Long, val descricao: String)

@Serializable
data class CriarAvaliacaoRequest(val nota: Int, val comentario: String? = null)

@Serializable
data class AtualizarFotoRequest(val url: String)

@Serializable
data class EnviarMensagemRequest(val conteudo: String)
```

### 9.2 Response Models

```kotlin
@Serializable
data class TokenResponse(val token: String)

@Serializable
data class MessageResponse(val mensagem: String)

@Serializable
data class ApiErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String
)

@Serializable
data class UserResponse(
    val id: Long,
    val nome: String,
    val email: String,
    val fotoUrl: String? = null
)

@Serializable
data class NegocioResponse(
    val id: Long,
    val nome: String,
    val categoria: String,
    val usuarioId: Long,
    val nomeUsuario: String,
    val ativo: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val enderecoCompleto: String? = null,
    val notaMedia: Double = 0.0,
    val logoUrl: String? = null
)

@Serializable
data class PedidoResponse(
    val id: Long,
    val clienteId: Long,
    val clienteNome: String,
    val negocioId: Long,
    val negocioNome: String,
    val descricao: String,
    val dataDesejada: String? = null,
    val status: String,  // ABERTO, ACEITO, RECUSADO, CONCLUIDO, CANCELADO
    val criadoEm: String
)

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int
)

@Serializable
data class AvaliacaoResponse(
    val id: Long,
    val nota: Int,
    val comentario: String? = null,
    val dataAvaliacao: String,
    val pedidoId: Long,
    val avaliadorNome: String,
    val avaliadoNome: String,
    val negocioId: Long,
    val negocioNome: String
)

@Serializable
data class MensagemResponse(
    val id: Long,
    val pedidoServicoId: Long,
    val remetenteId: Long,
    val remetenteNome: String,
    val conteudo: String,
    val enviadoEm: String,
    val lida: Boolean,
    val lidaEm: String? = null,
    val remetenteFotoUrl: String? = null
)
```

---

## 10. Status Codes and Business Rules

### 10.1 Order State Machine

```
                    +--------------+
                    |   ABERTO     |  <- Client creates order
                    +------+-------+
                           |
              +------------+------------+
              |            |            |
              v            v            v
      +-----------+ +-----------+ +----------+
      |  ACEITO   | | RECUSADO  | |CANCELADO |
      |(provider) | |(provider) | |(client)  |
      +-----+-----+ +-----------+ +----------+
            |
            v
      +-----------+
      | CONCLUIDO |  <- Provider marks as done
      |(provider) |
      +-----+-----+
            |
            v
      +-----------+
      | AVALIADO  |  <- Client leaves review
      |(client)   |
      +-----------+
```

### 10.2 Permission Matrix

| Action            | Client | Provider (Business Owner) |
|-------------------|--------|---------------------------|
| Create order      | Yes    | No                        |
| Accept order      | No     | Yes                       |
| Decline order     | No     | Yes                       |
| Complete order    | No     | Yes                       |
| Cancel order      | Yes    | No                        |
| Send message      | Yes    | Yes                       |
| View chat history | Yes    | Yes                       |
| Create review     | Yes    | No                        |

### 10.3 Valid State Transitions

| From   | To        | Who      | Endpoint                       |
|--------|-----------|----------|--------------------------------|
| ABERTO | ACEITO    | Provider | `PATCH /pedidos/{id}/aceitar`  |
| ABERTO | RECUSADO  | Provider | `PATCH /pedidos/{id}/recusar`  |
| ABERTO | CANCELADO | Client   | `PATCH /pedidos/{id}/cancelar` |
| ACEITO | CONCLUIDO | Provider | `PATCH /pedidos/{id}/concluir` |

### 10.4 UI State for Order Cards

```
kotlin
enum class OrderStatus(val label: String, val color: Color) {
    ABERTO("Aguardando", Color(0xFFFFA726)),      // Orange
    ACEITO("Em andamento", Color(0xFF42A5F5)),     // Blue
    RECUSADO("Recusado", Color(0xFFEF5350)),       // Red
    CONCLUIDO("Concluido", Color(0xFF66BB6A)),     // Green
    CANCELADO("Cancelado", Color(0xFF9E9E9E))      // Gray
}

```

---

## 11. Testing Checklist

### For the Frontend Engineer

Before each PR, verify these flows work end-to-end:

**Auth:**
- [ ] Register new user -> auto-login -> see home
- [ ] Login with valid credentials -> see home
- [ ] Login with wrong password -> see error message
- [ ] Register with duplicate email -> see error message
- [ ] Access protected route without token -> redirect to login
- [ ] Forgot password -> receive code -> reset password -> login

**Businesses:**
- [ ] Home screen loads business list (with GPS)
- [ ] Search by name returns filtered results
- [ ] Business detail screen loads correctly

**Orders:**
- [ ] Client creates order -> status ABERTO
- [ ] Provider accepts -> status ACEITO
- [ ] Provider completes -> status CONCLUIDO
- [ ] Client cancels ABERTO order -> status CANCELADO
- [ ] Provider CANNOT cancel (button hidden or disabled)
- [ ] Provider CANNOT complete ABERTO order (only ACEITO)
- [ ] Pagination works on orders list

**Chat:**
- [ ] Chat history loads when entering screen
- [ ] WebSocket connects successfully
- [ ] Send message -> appears in real-time for both users
- [ ] Typing indicator works
- [ ] Disconnect when leaving screen

**Reviews:**
- [ ] Client can review CONCLUIDO order
- [ ] Cannot review same order twice
- [ ] Rating updates business `notaMedia`

---

## 12. Changelog

| Version | Date       | Changes                                                           |
|---------|------------|-------------------------------------------------------------------|
| 1.1.0   | 2026-02-15 | Fix endpoints, add password recovery, update DTOs, add pagination |
| 1.0.0   | 2026-02-06 | Initial release - complete V1 API documentation                   |

---

**Questions?** Contact Natanael Lopes (Backend) or open an issue on the repo.

**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`