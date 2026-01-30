# 📘 EasyBiz API — Contrato Oficial (Swagger-like)

Este documento define o **contrato estável da API** para consumo por aplicações Web e Mobile.

> ⚠️ Este arquivo é a fonte de verdade para o Front-end.
> O Swagger UI é complementar e serve apenas para execução/testes.

---

## 🌐 Base URL

**Ambiente Local**

```
http://localhost:8080
```

---

## 🔐 Autenticação

### Header obrigatório para rotas protegidas

```
Authorization: Bearer {JWT}
```

---

## 🔹 1) Autenticação

### POST `/auth/login`

Autentica o usuário e retorna o token JWT.

**Request**

```json
{
  "email": "usuario@dominio.com",
  "senha": "123456"
}
```

**Response — 200 OK**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

✔️ **Sem autenticação**

---

## 🔹 2) Usuários

### POST `/usuarios`

Cria um novo usuário (cliente ou potencial prestador).

**Request**

```json
{
  "nomeCompleto": "João Silva",
  "email": "joao@email.com",
  "senha": "123456"
}
```

**Response — 201 CREATED**

✔️ **Sem autenticação**

** PATCH /usuarios/me/foto **

Atualiza a foto de perfil do usuário autenticado.

Request:

``` 

{
  "url": "https://cdn.meuservico.com/avatar.png"
}
```
Response:
204 No Content


---

## 🔹 3) Negócios (Prestadores)

### POST `/negocios` 🔒

Cria um negócio vinculado ao usuário autenticado.

**Request**

```json
{
  "nome": "Barbearia do João",
  "descricao": "Cortes e barbas",
  "categoria": "BARBEARIA"
}
```

**Response — 201 CREATED**

```json
{
  "id": 10,
  "nome": "Barbearia do João",
  "descricao": "Cortes e barbas"
}
```

** PATCH /negocios/{id}/logo ** 

Atualiza a logo do negócio.


Regras:
- Apenas o dono do negócio pode atualizar

Request:

```
{
  "url": "https://cdn.meuservico.com/logo.png"
}

```
Response:
204 No Content

---

### GET `/negocios`

Lista negócios disponíveis.

**Query Params (opcional)**

```
/negocios?nome=barbearia
```

---

### GET `/negocios/{id}`

Retorna detalhes de um negócio específico.

---

## 🔹 4) Pedidos de Serviço (Sala de Negociação)

### POST `/pedidos` 🔒

Cria um novo pedido.

**Request**

```json
{
  "negocioId": 10,
  "descricao": "Cortar cabelo às 15h"
}
```

**Response**

```json
{
  "id": 55,
  "status": "ABERTO",
  "clienteId": 7,
  "negocioId": 10
}
```

---

### GET `/pedidos/{id}` 🔒

Retorna detalhes do pedido.

---

## 🔄 Workflow do Pedido

Estados possíveis:

* `ABERTO`
* `ACEITO`
* `RECUSADO`
* `CONCLUIDO`

---

### PATCH `/pedidos/{id}/aceitar` 🔒

✔️ Somente o dono do negócio

---

### PATCH `/pedidos/{id}/recusar` 🔒

✔️ Somente o dono do negócio

---

### PATCH `/pedidos/{id}/concluir` 🔒

✔️ Apenas se o pedido estiver ACEITO

---

### PATCH `/pedidos/{id}/cancelar` 🔒

✔️ Apenas o cliente

---

## 🔹 5) Mensagens — REST (Histórico)

### GET `/pedidos/{id}/mensagens` 🔒

Lista mensagens do chat.

---

## 🔹 6) WebSocket — Tempo Real

### Endpoint

```
ws://localhost:8080/ws-chat
```

---

## 🔹 7) Avaliações (Review)

### POST `/avaliacoes/pedido/{pedidoId}` 🔒

Cliente avalia o serviço.

---


## 🔹 8) Busca Inteligente de Negócios

### GET /negocios/busca

### Descrição
Busca negócios próximos ao usuário com priorização automática:

1. Melhor avaliação
2. Proximidade geográfica
3. Correção de erros comuns de digitação

### Query Params
- lat (Double) – latitude do usuário
- lon (Double) – longitude do usuário
- busca (String, opcional) – categoria aproximada

### Regras
- Caso a categoria não seja reconhecida, a busca retorna todos os negócios da região
- Resultados ordenados por nota média



## ❌ Padronização de Erros

```json
{
  "timestamp": "2026-01-27T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Acesso negado ao recurso",
  "path": "/pedidos/55/aceitar"
}
```
