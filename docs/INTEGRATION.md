# 📘 EasyBiz — API Integration Guide

**Version:** 1.0.0  
**Last Updated:** 2026-02-06  
**Backend:** Spring Boot 3.4 · Java 17 · PostgreSQL  
**Frontend:** Kotlin Multiplatform · Compose Multiplatform  
**Audience:** Frontend Engineers (Edson Neto)  
**Maintainer:** Natanael Lopes (Backend)

---

## Table of Contents

1. [Quick Start](#1-quick-start)
2. [Environments](#2-environments)
3. [Authentication](#3-authentication)
4. [API Reference](#4-api-reference)
   - 4.1 [Auth] (#41-auth)
   - 4.2 [Users] (#42-users)
   
 - 4.3 [Businesses Negocios] (#43-businesses-negocios)
   - 4.4 [Orders (Pedidos)] (#44-orders-pedidos)
   - 4.5 [Reviews (Avaliacoes)] (#45-reviews-avaliacoes)
   - 4.6 [Chat (Messages)] (#46-chat-messages)
5. [WebSocket - Real-Time Chat] (#5-websocket---real-time-chat)
6. [Error Handling](#6-error-handling)
7. [Screen-to-Endpoint Mapping](#7-screen-to-endpoint-mapping)
8. [Kotlin/Ktor Implementation Guide] (#8-kotlinktor-implementation-guide)
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

**First API call — Register + Login:**

```bash
# Register
curl -X POST http://localhost:8080/auth/cadastro \
  -H "Content-Type: application/json" \
  -d '{"nomeCompleto":"Edson Neto","email":"edson@test.com","senha":"123456"}'

# Login → get token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"edson@test.com","senha":"123456"}'

# Response: { "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

---

## 2. Environments

| Environment | Base URL | Status |
|-------------|----------|--------|
| Local | `http://localhost:8080` | ✅ Active |
| Staging | `https://easybiz-staging.up.railway.app` | 🔜 Planned |
| Production | `https://api.easybiz.com.br` | 🔜 Planned |

**Important:** All endpoints require HTTPS in staging/production. Local development uses HTTP.

---

## 3. Authentication

EasyBiz uses **JWT (JSON Web Token)** with stateless authentication. No sessions, no cookies.

### 3.1 Auth Flow

```
┌─────────┐         ┌─────────┐         ┌──────────┐
│  App     │         │  API    │         │ Database │
└────┬────┘         └────┬────┘         └────┬─────┘
     │                    │                   │
     │ POST /auth/login   │                   │
     │───────────────────>│                   │
     │                    │  Validate BCrypt  │
     │                    │──────────────────>│
     │                    │<──────────────────│
     │  { token: "eyJ.." }│                   │
     │<───────────────────│                   │
     │                    │                   │
     │ GET /usuarios/me   │                   │
     │ Authorization:     │                   │
     │ Bearer eyJ...      │                   │
     │───────────────────>│                   │
     │                    │  Extract userId   │
     │                    │  from token       │
     │  { id, nome, ... } │                   │
     │<───────────────────│                   │
```

### 3.2 Token Rules

| Property | Value |
|----------|-------|
| Algorithm | HS256 |
| Expiration | 24 hours (86400000 ms) |
| Header format | `Authorization: Bearer <token>` |
| Token content | userId embedded in subject claim |

### 3.3 How to Use the Token

Every request (except `/auth/**`) **must** include the token:

```
GET /usuarios/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzA3...
Content-Type: application/json
```

### 3.4 Token Expiration Handling

When the token expires, the API returns `401 Unauthorized`. The app should:

1. Detect `401` response
2. Redirect user to login screen
3. Clear stored token
4. After re-login, retry the failed request

> **Note (V1):** There is no refresh token mechanism. The user must re-login after 24h. This is planned for V1.1.

---

## 4. API Reference

### Base Headers (all requests)

```
Content-Type: application/json
Authorization: Bearer <token>    # except /auth/** endpoints
```

---

### 4.1 Auth

#### `POST /auth/cadastro` — Register User

**Auth required:** No

**Request:**
```json
{
  "nomeCompleto": "Edson Neto",
  "email": "edson@email.com",
  "senha": "minhasenha123"
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "nomeCompleto": "Edson Neto",
  "email": "edson@email.com"
}
```

**Error `400 Bad Request`:**
```json
{
  "error": "Email já cadastrado"
}
```

**Validation rules:**
- `nomeCompleto`: required, min 2 chars
- `email`: required, valid email format, unique
- `senha`: required, min 6 chars

---

#### `POST /auth/login` — Login

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
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzA3MjM0NTY3LCJleHAiOjE3MDczMjA5Njd9.abc123"
}
```

**Error `400 Bad Request`:**
```json
{
  "error": "Credenciais inválidas"
}
```

> **Security:** Password is validated with BCrypt. The API never reveals whether the email exists or the password is wrong — always returns "Credenciais inválidas" for both cases.

---

### 4.2 Users

#### `GET /usuarios/me` — Get Current User Profile

**Auth required:** Yes

**Response `200 OK`:**
```json
{
  "id": 1,
  "nomeCompleto": "Edson Neto",
  "email": "edson@email.com",
  "fotoUrl": "https://example.com/foto.jpg"
}
```

**Error `401 Unauthorized`:** Token missing or expired

> **Frontend note:** Call this endpoint immediately after login to load the user profile, avatar, and permissions. Store the response in local state.

---

### 4.3 Businesses (Negocios)

#### `POST /negocios` — Create Business

**Auth required:** Yes (owner becomes the authenticated user)

**Request:**
```json
{
  "nome": "Barbearia do Edson",
  "descricao": "Cortes modernos e barba",
  "categoria": "BARBEARIA",
  "endereco": "Rua das Flores, 123 - São Paulo",
  "latitude": -23.5505,
  "longitude": -46.6333,
  "telefone": "11999999999"
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "nome": "Barbearia do Edson",
  "descricao": "Cortes modernos e barba",
  "categoria": "BARBEARIA",
  "endereco": "Rua das Flores, 123 - São Paulo",
  "latitude": -23.5505,
  "longitude": -46.6333,
  "telefone": "11999999999",
  "logoUrl": null,
  "notaMedia": 0.0,
  "totalAvaliacoes": 0,
  "dono": {
    "id": 1,
    "nomeCompleto": "Edson Neto"
  }
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

#### `GET /negocios` — List All Businesses

**Auth required:** No

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "nome": "Barbearia do Edson",
    "descricao": "Cortes modernos e barba",
    "categoria": "BARBEARIA",
    "logoUrl": "https://example.com/logo.jpg",
    "notaMedia": 4.5,
    "totalAvaliacoes": 12,
    "latitude": -23.5505,
    "longitude": -46.6333,
    "dono": {
      "id": 1,
      "nomeCompleto": "Edson Neto"
    }
  }
]
```

---

#### `GET /negocios/{id}` — Get Business by ID

**Auth required:** No

**Response `200 OK`:** Same structure as above (single object)

**Error `404 Not Found`:**
```json
{
  "error": "Negócio não encontrado"
}
```

---

#### `GET /negocios/busca?nome={query}&latitude={lat}&longitude={lng}` — Smart Search

**Auth required:** No

**Parameters:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `nome` | string | No | Search by name (partial match) |
| `latitude` | double | No | User's current latitude |
| `longitude` | double | No | User's current longitude |
| `categoria` | string | No | Filter by category enum |

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "nome": "Barbearia do Edson",
    "categoria": "BARBEARIA",
    "notaMedia": 4.5,
    "distanciaKm": 2.3,
    "logoUrl": "https://example.com/logo.jpg"
  }
]
```

> **How it works:** Uses Haversine formula in PostgreSQL to calculate distance. Default radius is 30km. Results are sorted by rating and distance.

> **Frontend note:** Request location permission from the user, then pass their GPS coordinates to this endpoint.

---

#### `PATCH /negocios/{id}/logo` — Update Business Logo

**Auth required:** Yes (must be the business owner)

**Request:**
```json
{
  "logoUrl": "https://example.com/new-logo.jpg"
}
```

**Response `200 OK`:** Updated business object

**Error `403 Forbidden`:** If authenticated user is not the owner

---

### 4.4 Orders (Pedidos)

#### `POST /pedidos` — Create Order (Client → Business)

**Auth required:** Yes (authenticated user becomes the client)

**Request:**
```json
{
  "negocioId": 1,
  "descricao": "Corte de cabelo degradê + barba"
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "descricao": "Corte de cabelo degradê + barba",
  "status": "ABERTO",
  "criadoEm": "2026-02-06T14:30:00",
  "cliente": {
    "id": 2,
    "nomeCompleto": "João Silva"
  },
  "negocio": {
    "id": 1,
    "nome": "Barbearia do Edson"
  }
}
```

> **Business rule:** A new order creates a unique "negotiation room" (chat channel). Each order has its own independent chat history.

---

#### `GET /pedidos/meus` — List My Orders

**Auth required:** Yes

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "descricao": "Corte degradê + barba",
    "status": "ACEITO",
    "criadoEm": "2026-02-06T14:30:00",
    "cliente": { "id": 2, "nomeCompleto": "João Silva" },
    "negocio": { "id": 1, "nome": "Barbearia do Edson" }
  },
  {
    "id": 2,
    "descricao": "Pintura residencial",
    "status": "ABERTO",
    "criadoEm": "2026-02-05T10:00:00",
    "cliente": { "id": 2, "nomeCompleto": "João Silva" },
    "negocio": { "id": 3, "nome": "Pintura Express" }
  }
]
```

> **Note:** Returns all orders where the user is either the client OR the business owner.

---

#### `PATCH /pedidos/{id}/aceitar` — Accept Order

**Auth required:** Yes (must be the business owner)

**Response `200 OK`:**
```json
{
  "id": 1,
  "status": "ACEITO",
  "...": "..."
}
```

**Error `403 Forbidden`:** Not the business owner
**Error `400 Bad Request`:** Order is not in `ABERTO` status

---

#### `PATCH /pedidos/{id}/recusar` — Decline Order

**Auth required:** Yes (must be the business owner)

**Response `200 OK`:** Order with status `RECUSADO`

---

#### `PATCH /pedidos/{id}/concluir` — Complete Order

**Auth required:** Yes (must be the business owner)

**Response `200 OK`:** Order with status `CONCLUIDO`

**Error `400 Bad Request`:** Order is not in `ACEITO` status

> **Business rule:** Cannot complete an order that was not accepted first. The flow is strictly: ABERTO → ACEITO → CONCLUIDO.

---

#### `PATCH /pedidos/{id}/cancelar` — Cancel Order

**Auth required:** Yes (must be the client)

**Response `200 OK`:** Order with status `CANCELADO`

**Error `403 Forbidden`:** Only the client can cancel. If the provider tries to cancel, returns `4xx Client Error`.

> **Business rule:** Only the CLIENT who created the order can cancel it. The provider CANNOT cancel — they can only decline (recusar).

---

### 4.5 Reviews (Avaliacoes)

#### `POST /avaliacoes` — Create Review

**Auth required:** Yes (must be the client of the order)

**Request:**
```json
{
  "pedidoId": 1,
  "nota": 5,
  "comentario": "Excelente serviço, recomendo!"
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "nota": 5,
  "comentario": "Excelente serviço, recomendo!",
  "criadoEm": "2026-02-06T16:00:00",
  "cliente": { "id": 2, "nomeCompleto": "João Silva" },
  "negocio": { "id": 1, "nome": "Barbearia do Edson" }
}
```

**Validation rules:**
- `nota`: integer, 1-5
- `pedidoId`: must be a `CONCLUIDO` order
- Only one review per order
- Only the client can review

**Error `400 Bad Request`:**
```json
{
  "error": "Pedido já foi avaliado"
}
```

> **Side effect:** After creating a review, the business's `notaMedia` and `totalAvaliacoes` are automatically recalculated.

---

#### `GET /avaliacoes/negocio/{negocioId}` — List Reviews for a Business

**Auth required:** No

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "nota": 5,
    "comentario": "Excelente serviço!",
    "criadoEm": "2026-02-06T16:00:00",
    "cliente": { "id": 2, "nomeCompleto": "João Silva" }
  }
]
```

---

### 4.6 Chat (Messages)

#### `GET /pedidos/{pedidoId}/mensagens` — Load Chat History

**Auth required:** Yes (must be client or business owner of the order)

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "conteudo": "Olá, gostaria de agendar para sábado",
    "remetenteId": 2,
    "pedidoId": 1,
    "criadoEm": "2026-02-06T14:35:00"
  },
  {
    "id": 2,
    "conteudo": "Claro! Sábado às 10h pode ser?",
    "remetenteId": 1,
    "pedidoId": 1,
    "criadoEm": "2026-02-06T14:36:00"
  }
]
```

**Error `403 Forbidden`:** User is not a participant of this order (IDOR protection)

> **Important:** Always load history via REST first when entering the chat screen, then connect WebSocket for real-time messages.

---

## 5. WebSocket - Real-Time Chat

### 5.1 Architecture

```
┌────────────┐    STOMP/SockJS    ┌─────────────┐
│  App        │ ◄───────────────► │  Backend     │
│  (Client)   │    /ws-chat       │  (Broker)    │
└──────┬─────┘                    └──────┬──────┘
       │                                  │
       │  SUBSCRIBE                       │
       │  /topic/mensagens/{pedidoId}     │
       │─────────────────────────────────>│
       │                                  │
       │  SEND                            │
       │  /app/chat.enviar                │
       │  { pedidoId, conteudo }          │
       │─────────────────────────────────>│
       │                                  │ 1. Validate JWT
       │                                  │ 2. Extract userId
       │                                  │ 3. Save to DB
       │  MESSAGE                         │ 4. Broadcast
       │  /topic/mensagens/{pedidoId}     │
       │  { id, conteudo, remetenteId }   │
       │<─────────────────────────────────│
```

### 5.2 Connection

| Property | Value |
|----------|-------|
| Endpoint | `ws://localhost:8080/ws-chat` (dev) / `wss://api.easybiz.com.br/ws-chat` (prod) |
| Protocol | STOMP over SockJS |
| Auth | `Authorization: Bearer <token>` header during CONNECT |

### 5.3 Connection Flow

```
1. CONNECT with JWT header
2. SUBSCRIBE to /topic/mensagens/{pedidoId}
3. SEND messages to /app/chat.enviar
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
destination:/app/chat.enviar
content-type:application/json

{"pedidoId":1,"conteudo":"Olá!"}
\0
```

**Receive message (from server):**
```
MESSAGE
destination:/topic/mensagens/1
content-type:application/json

{"id":45,"conteudo":"Olá!","remetenteId":2,"pedidoId":1,"criadoEm":"2026-02-06T14:35:00"}
```

### 5.5 Additional Real-Time Features

**Typing indicator:**
```
SEND
destination:/app/chat.digitando
content-type:application/json

{"pedidoId":1}
```

Subscribe: `/topic/digitando/{pedidoId}`

**Read confirmation:**
```
SEND
destination:/app/chat.leitura
content-type:application/json

{"pedidoId":1}
```

Subscribe: `/topic/leitura/{pedidoId}`

### 5.6 Security

- JWT is validated during the WebSocket handshake (CONNECT frame)
- If the token is invalid or expired, the connection is rejected with `403 Forbidden`
- The sender ID (`remetenteId`) is ALWAYS extracted from the token — never from the request body
- This prevents spoofing (a user cannot send messages as another user)

---

## 6. Error Handling

### 6.1 Standard Error Response

All errors follow this format:

```json
{
  "error": "Human-readable error message in Portuguese"
}
```

### 6.2 HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| `200` | OK | Successful GET, PATCH |
| `201` | Created | Successful POST (new resource) |
| `400` | Bad Request | Validation error, business rule violation |
| `401` | Unauthorized | Missing/expired token |
| `403` | Forbidden | Valid token but no permission (IDOR) |
| `404` | Not Found | Resource doesn't exist |
| `500` | Internal Error | Unexpected server error |

### 6.3 Common Error Messages

| Error | Cause | Frontend Action |
|-------|-------|-----------------|
| `"Credenciais inválidas"` | Wrong email/password | Show error on login form |
| `"Email já cadastrado"` | Duplicate email | Show error on register form |
| `"Negócio não encontrado"` | Invalid business ID | Show "not found" screen |
| `"Pedido não encontrado"` | Invalid order ID | Navigate back |
| `"Pedido já foi avaliado"` | Duplicate review | Disable review button |
| `"Apenas o cliente pode cancelar"` | Provider tried to cancel | Hide cancel button for providers |
| `"Pedido não está ABERTO"` | Invalid status transition | Refresh order status |
| `"Acesso negado"` | IDOR attempt | Navigate back, show error toast |

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
                // Token expired → redirect to login
                TokenManager.clear()
                ApiResult.Error(401, "Sessão expirada")
            }
            403 -> ApiResult.Error(403, "Acesso negado")
            else -> {
                val body = e.response.bodyAsText()
                val msg = Json.decodeFromString<ErrorResponse>(body).error
                ApiResult.Error(e.response.status.value, msg)
            }
        }
    } catch (e: Exception) {
        ApiResult.Error(0, "Erro de conexão")
    }
}
```

---

## 7. Screen-to-Endpoint Mapping

This maps every app screen to the exact API calls needed.

### 7.1 Splash / App Init

```
App opens
  └─► Check if token exists in local storage
       ├─► YES → GET /usuarios/me
       │         ├─► 200 → Navigate to Home
       │         └─► 401 → Navigate to Login
       └─► NO  → Navigate to Login
```

### 7.2 Login Screen

```
User taps "Entrar"
  └─► POST /auth/login { email, senha }
       ├─► 200 → Store token → GET /usuarios/me → Navigate to Home
       └─► 400 → Show "Credenciais inválidas"
```

### 7.3 Register Screen

```
User taps "Cadastrar"
  └─► POST /auth/cadastro { nomeCompleto, email, senha }
       ├─► 201 → POST /auth/login (auto-login) → Navigate to Home
       └─► 400 → Show "Email já cadastrado"
```

### 7.4 Home Screen (Search)

```
Screen loads
  └─► Request GPS permission
       └─► GET /negocios/busca?latitude={lat}&longitude={lng}
            └─► Render list of businesses sorted by distance + rating

User types in search bar
  └─► GET /negocios/busca?nome={query}&latitude={lat}&longitude={lng}

User selects category filter
  └─► GET /negocios/busca?categoria={BARBEARIA}&latitude={lat}&longitude={lng}
```

### 7.5 Business Detail Screen

```
User taps a business card
  └─► GET /negocios/{id}
       └─► Render business details (name, description, rating, reviews)

Load reviews section
  └─► GET /avaliacoes/negocio/{id}
       └─► Render review cards

User taps "Solicitar Serviço"
  └─► POST /pedidos { negocioId, descricao }
       ├─► 201 → Navigate to Chat Screen (with pedidoId)
       └─► 400 → Show error
```

### 7.6 My Orders Screen

```
Screen loads
  └─► GET /pedidos/meus
       └─► Render order cards grouped by status

Order card shows:
  - Business name + logo
  - Description
  - Status badge (color-coded)
  - Created date

User taps an order
  └─► Navigate to Chat Screen (with pedidoId)
```

### 7.7 Chat Screen

```
Screen loads
  ├─► GET /pedidos/{pedidoId}/mensagens  (load history)
  └─► CONNECT WebSocket /ws-chat (with JWT)
       └─► SUBSCRIBE /topic/mensagens/{pedidoId}

User sends message
  └─► SEND /app/chat.enviar { pedidoId, conteudo }

User typing
  └─► SEND /app/chat.digitando { pedidoId }

Receive message (real-time)
  └─► MESSAGE from /topic/mensagens/{pedidoId}
       └─► Append to message list, scroll to bottom

Screen closes
  └─► DISCONNECT WebSocket
```

### 7.8 Provider Actions (Order Management)

```
Provider sees new order (status: ABERTO)
  ├─► "Aceitar"  → PATCH /pedidos/{id}/aceitar
  └─► "Recusar"  → PATCH /pedidos/{id}/recusar

Provider finishes service (status: ACEITO)
  └─► "Concluir" → PATCH /pedidos/{id}/concluir

Client sees completed order (status: CONCLUIDO)
  └─► "Cancelar" → PATCH /pedidos/{id}/cancelar  (only if ABERTO)
  └─► "Avaliar"  → Navigate to Review Screen
```

### 7.9 Review Screen

```
User submits review
  └─► POST /avaliacoes { pedidoId, nota, comentario }
       ├─► 201 → Show success → Navigate back
       └─► 400 → Show "Pedido já foi avaliado"
```

### 7.10 Profile / My Business Screen

```
Screen loads
  └─► GET /usuarios/me
       └─► Render user info

If user is a provider
  └─► Load their business details

Update logo
  └─► PATCH /negocios/{id}/logo { logoUrl }
```

---

## 8. Kotlin/Ktor Implementation Guide

### 8.1 Ktor Client Setup (KMP)

```kotlin
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
                url("http://10.0.2.2:8080") // Android emulator → localhost
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

```kotlin
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

```kotlin
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

    suspend fun cadastro(nome: String, email: String, senha: String): UserResponse {
        return client.post("/auth/cadastro") {
            setBody(CadastroRequest(nome, email, senha))
        }.body()
    }

    suspend fun getMe(): UserResponse {
        return client.get("/usuarios/me").body()
    }
}
```

### 8.4 WebSocket Client (STOMP over SockJS)

```kotlin
// core/networking/WebSocketManager.kt

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

class ChatWebSocketManager(
    private val tokenProvider: () -> String?
) {
    private var session: WebSocketSession? = null
    private val _messages = MutableSharedFlow<MensagemResponse>()
    val messages: SharedFlow<MensagemResponse> = _messages

    private val client = HttpClient {
        install(WebSockets)
    }

    suspend fun connect(pedidoId: Long) {
        val token = tokenProvider() ?: throw IllegalStateException("No token")

        // Connect with STOMP over SockJS
        // Note: For KMP, consider using a STOMP library like:
        // - krossbow (https://github.com/joffrey-bion/krossbow)
        // - Or implement raw WebSocket with STOMP frame parsing

        // Using krossbow (recommended for KMP):
        // val stompClient = StompClient(WebSocketClient.builtIn())
        // val stompSession = stompClient.connect(
        //     "ws://10.0.2.2:8080/ws-chat",
        //     customStompConnectHeaders = mapOf("Authorization" to "Bearer $token")
        // )
        //
        // val subscription = stompSession.subscribe("/topic/mensagens/$pedidoId")
        // subscription.collect { frame ->
        //     val message = Json.decodeFromString<MensagemResponse>(frame.bodyAsText)
        //     _messages.emit(message)
        // }
    }

    suspend fun sendMessage(pedidoId: Long, conteudo: String) {
        // stompSession.send("/app/chat.enviar", """{"pedidoId":$pedidoId,"conteudo":"$conteudo"}""")
    }

    suspend fun sendTyping(pedidoId: Long) {
        // stompSession.send("/app/chat.digitando", """{"pedidoId":$pedidoId}""")
    }

    fun disconnect() {
        // stompSession.disconnect()
    }
}
```

> **Recommended library for STOMP in KMP:** [Krossbow](https://github.com/joffrey-bion/krossbow) — handles STOMP frames, heartbeats, and reconnection out of the box.

### 8.5 Koin DI Module

```kotlin
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
data class CadastroRequest(val nomeCompleto: String, val email: String, val senha: String)

@Serializable
data class CriarNegocioRequest(
    val nome: String,
    val descricao: String,
    val categoria: String,
    val endereco: String,
    val latitude: Double,
    val longitude: Double,
    val telefone: String
)

@Serializable
data class CriarPedidoRequest(val negocioId: Long, val descricao: String)

@Serializable
data class CriarAvaliacaoRequest(val pedidoId: Long, val nota: Int, val comentario: String)

@Serializable
data class AtualizarLogoRequest(val logoUrl: String)

@Serializable
data class EnviarMensagemRequest(val pedidoId: Long, val conteudo: String)
```

### 9.2 Response Models

```kotlin
@Serializable
data class TokenResponse(val token: String)

@Serializable
data class ErrorResponse(val error: String)

@Serializable
data class UserResponse(
    val id: Long,
    val nomeCompleto: String,
    val email: String,
    val fotoUrl: String? = null
)

@Serializable
data class DonoResponse(val id: Long, val nomeCompleto: String)

@Serializable
data class NegocioResponse(
    val id: Long,
    val nome: String,
    val descricao: String,
    val categoria: String,
    val endereco: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val telefone: String? = null,
    val logoUrl: String? = null,
    val notaMedia: Double = 0.0,
    val totalAvaliacoes: Int = 0,
    val distanciaKm: Double? = null,
    val dono: DonoResponse? = null
)

@Serializable
data class PedidoResponse(
    val id: Long,
    val descricao: String,
    val status: String,  // ABERTO, ACEITO, RECUSADO, CONCLUIDO, CANCELADO
    val criadoEm: String,
    val cliente: DonoResponse,
    val negocio: NegocioSimplificadoResponse
)

@Serializable
data class NegocioSimplificadoResponse(val id: Long, val nome: String)

@Serializable
data class AvaliacaoResponse(
    val id: Long,
    val nota: Int,
    val comentario: String,
    val criadoEm: String,
    val cliente: DonoResponse
)

@Serializable
data class MensagemResponse(
    val id: Long,
    val conteudo: String,
    val remetenteId: Long,
    val pedidoId: Long,
    val criadoEm: String
)
```

---

## 10. Status Codes and Business Rules

### 10.1 Order State Machine

```
                    ┌──────────────┐
                    │   ABERTO     │  ← Client creates order
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
      ┌───────────┐ ┌───────────┐ ┌──────────┐
      │  ACEITO   │ │ RECUSADO  │ │CANCELADO │
      │(provider) │ │(provider) │ │(client)  │
      └─────┬─────┘ └───────────┘ └──────────┘
            │
            ▼
      ┌───────────┐
      │ CONCLUIDO │  ← Provider marks as done
      │(provider) │
      └─────┬─────┘
            │
            ▼
      ┌───────────┐
      │ AVALIADO  │  ← Client leaves review
      │(client)   │
      └───────────┘
```

### 10.2 Permission Matrix

| Action | Client | Provider (Business Owner) |
|--------|--------|---------------------------|
| Create order | ✅ | ❌ |
| Accept order | ❌ | ✅ |
| Decline order | ❌ | ✅ |
| Complete order | ❌ | ✅ |
| Cancel order | ✅ | ❌ |
| Send message | ✅ | ✅ |
| View chat history | ✅ | ✅ |
| Create review | ✅ | ❌ |
| View reviews | ✅ | ✅ |

### 10.3 Valid State Transitions

| From | To | Who | Endpoint |
|------|----|-----|----------|
| ABERTO | ACEITO | Provider | `PATCH /pedidos/{id}/aceitar` |
| ABERTO | RECUSADO | Provider | `PATCH /pedidos/{id}/recusar` |
| ABERTO | CANCELADO | Client | `PATCH /pedidos/{id}/cancelar` |
| ACEITO | CONCLUIDO | Provider | `PATCH /pedidos/{id}/concluir` |

### 10.4 UI State for Order Cards

```kotlin
enum class OrderStatus(val label: String, val color: Color) {
    ABERTO("Aguardando", Color(0xFFFFA726)),      // Orange
    ACEITO("Em andamento", Color(0xFF42A5F5)),     // Blue
    RECUSADO("Recusado", Color(0xFFEF5350)),       // Red
    CONCLUIDO("Concluído", Color(0xFF66BB6A)),     // Green
    CANCELADO("Cancelado", Color(0xFF9E9E9E))      // Gray
}
```

---

## 11. Testing Checklist

### For the Frontend Engineer

Before each PR, verify these flows work end-to-end:

**Auth:**
- [ ] Register new user → auto-login → see home
- [ ] Login with valid credentials → see home
- [ ] Login with wrong password → see error message
- [ ] Register with duplicate email → see error message
- [ ] Access protected route without token → redirect to login

**Businesses:**
- [ ] Home screen loads business list
- [ ] Search by name returns filtered results
- [ ] Search with GPS returns sorted by distance
- [ ] Category filter works
- [ ] Business detail screen loads correctly

**Orders:**
- [ ] Client creates order → status ABERTO
- [ ] Provider accepts → status ACEITO
- [ ] Provider completes → status CONCLUIDO
- [ ] Client cancels ABERTO order → status CANCELADO
- [ ] Provider CANNOT cancel (button hidden or disabled)
- [ ] Provider CANNOT complete ABERTO order (only ACEITO)

**Chat:**
- [ ] Chat history loads when entering screen
- [ ] WebSocket connects successfully
- [ ] Send message → appears in real-time for both users
- [ ] Typing indicator works
- [ ] Disconnect when leaving screen

**Reviews:**
- [ ] Client can review CONCLUIDO order
- [ ] Cannot review same order twice
- [ ] Rating updates business `notaMedia`
- [ ] Review appears on business detail screen

---

## 12. Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-02-06 | Initial release — complete V1 API documentation |

---

**Questions?** Contact Natanael Lopes (Backend) or open an issue on the repo.

**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

**Postman Collection:** (TODO: export and add link)
