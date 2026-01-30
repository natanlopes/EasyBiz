## 📘 EasyBiz API – Contrato Completo para Consumo (Swagger-like)



> Current Version: **v1**
> Last Update: 2026-01-30

## Version
- v1.0 – Chat, Pedidos, Autenticação JWT
- v2.0 – Pagamentos, Avaliações, Notificações Push

## 📦 API v1 – Chat e Pedidos

Base URL (local):

```
http://localhost:8080
```

Todos os endpoints protegidos exigem o header:

```
Authorization: Bearer {JWT}

```

### 🔹 1) Autenticação
📌 POST /auth/login

Autentica e retorna token JWT.

Request

```
{
  "email": "usuario@dominio.com",
  "senha": "123456"
}


```

Response 200

```
{
  "token": "eyJhbGc..."
}

```

## ✔️ Sem autenticação.

🔹 2) Usuários
📌 POST /usuarios

Cria um novo usuário (cliente ou potencial prestador).

Request

```
{
  "nomeCompleto": "João Silva",
  "email": "joao@email.com",
  "senha": "123456"
}
```

Response
201 CREATED

📌 Sem autenticação (perfil público para cadastro).

## 🔹 3) Negócios (Prestadores)
📌 POST /negocios 🔒

Cria negócio vinculado ao usuário autenticado.

Request

```
{
  "nome": "Barbearia do João",
  "descricao": "Cortes e barbas",
  "categoria": "BARBEARIA"
}
```

Response 201

```
{
  "id": 10,
  "nome": "Barbearia do João",
  "descricao": "Cortes e barbas",
  ...
}
```

## 📌 GET /negocios

Lista todos os negócios disponíveis.

Query (opcional)
Exemplo:

/negocios?nome=barbearia


Response

```
[
  {
    "id": 10,
    "nome": "Barbearia do João",
    ...
  }
]
```

## 📌 GET /negocios/{id}

Retorna detalhes de um negócio específico.

Response

```
{
  "id": 10,
  "nome": "Barbearia do João",
  ...
}
```

## 🔹 4) Pedidos de Serviço
📌 POST /pedidos 🔒

Cria um pedido (nova sala de negociação).

Request

```
{
  "negocioId": 10,
  "descricao": "Cortar cabelo às 15h"
}
```

Response

```
{
  "id": 55,
  "status": "ABERTO",
  "clienteId": 7,
  "negocioId": 10
}
```

## 📌 GET /pedidos/{id} 🔒

Retorna os detalhes do pedido específico.

Response

```
{
  "id": 55,
  "status": "ABERTO",
  "descricao": "...",
  "clienteId": 7,
  "negocioId": 10
}
```

## 🔄 Workflow do Pedido (Status)

## ➡ Todos esses endpoints exigem JWT e validação de autorização de negócio/cliente.

## 📌 PATCH /pedidos/{id}/aceitar 🔒

Prestador aceita o pedido.

Response

```
{
  "id": 55,
  "status": "ACEITO"
}
```


## ✔️ Somente o dono do negócio pode chamar.

📌 PATCH /pedidos/{id}/recusar 🔒

Prestador rejeita o pedido.

Response

```
{
  "id": 55,
  "status": "RECUSADO"
}
```



## 📌 PATCH /pedidos/{id}/concluir 🔒

Finaliza o pedido de serviço.

Response

```

{
  "id": 55,
  "status": "CONCLUIDO"
}
```

## ✔️ Só pode ser feito se o pedido já tiver sido ACEITO.

- 📌 PATCH /pedidos/{id}/cancelar 🔒

Cliente cancela pedido.

Response
204 NO CONTENT

##🔹 5) Mensagens – REST (Histórico)
- 📌 GET /pedidos/{id}/mensagens 🔒

Retorna lista de mensagens do chat do pedido.

Response

```
[
  {
    "id": 99,
    "pedidoServicoId": 55,
    "remetenteId": 7,
    "conteudo": "Olá!",
    ...
  },
  ...
]
```

✔️ Apenas Cliente e Prestador.

## 🔹 6) WebSocket – Tempo Real
Endpoint de conexão
ws://localhost:8080/ws-chat


Headers:

Authorization: Bearer {JWT}

Subscriptions (STOMP)
Ação	Tópico	Payload

```
Ouvir mensagens	/topic/mensagens/{pedidoId}	Mensagem tempo real
Ouvir “digitando”	/topic/mensagens/{pedidoId}/digitando	{"usuarioId", "usuarioNome","digitando":true/false}
Ouvir leitura	/topic/mensagens/{pedidoId}/lida	{"mensagemId","quemLeuId","pedidoId","lidoEm"}
Ouvir último visto	/topic/mensagens/{pedidoId}/ultimo-visto	{"pedidoId","vistoEm"}
Enviar via STOMP
📌 Enviar mensagem
/app/chat/{pedidoId}
```

Payload:

```

{
  "conteudo": "Mensagem do usuário"
}
```

✔ O backend ignora campo usuarioId no WS — usa o do token.

📌 “Digitando”
/app/chat/{pedidoId}/digitando


Payload:

```
{
  "usuarioId": 7,
  "usuarioNome": "Cliente",
  "digitando": true
}
```
## 📌 Marcar como lida (event)
/app/chat/{pedidoId}/lida/{mensagemId}


## Payload:

{"usuarioId": 7}


Emitido para /topic/mensagens/{pedidoId}/lida

## 🔹 7) Regras de segurança da API

✔ Rota protegida se não estiver em /auth ou /usuarios ➜ JWT obrigatório
✔ Token deve ser válido e não expirado
✔ Acesso a pedido/chat só permitido a participante do pedido
✔ WebSocket validado no handshake com token


- ✨ Padronização dos responses

Retornar mensagens de erro com estrutura:

```
{
  "timestamp": "...",
  "status": 403,
  "error": "Forbidden",
  "message": "Mensagem de erro legível",
  "path": "/pedidos/55/aceitar"
}
```

## 🔹 8) Avaliações (Review)
📌 POST /avaliacoes/pedido/{pedidoId} 🔒

Cliente avalia o serviço prestado.

**Pré-requisito:** O pedido deve estar com status `CONCLUIDO`.

**Request:**

```
{
  "nota": 5,
  "comentario": "Excelente profissional, muito rápido!"
}

```
Response (200 OK):

```
{
  "id": 1,
  "nota": 5,
  "comentario": "Excelente profissional...",
  "dataAvaliacao": "2026-01-27T10:00:00"
}
```

Para facilitar migração.

📍 Validação

Todos os recursos que lidam com dados sensíveis devem retornar 400 quando o corpo é inválido e 401 quando o token é inválido.

📚 Referência interativa

Swagger UI local:
👉 http://localhost:8080/swagger-ui/index.html



---
## 🆕 Infraestrutura (V1 Final)

### 🔐 GET /usuarios/me
Retorna os dados do usuário autenticado com base no JWT.

Response:
{
  "id": 1,
  "nomeCompleto": "Marcos Silva",
  "email": "marcos@email.com",
  "fotoUrl": "https://cdn.app/avatar.png"
}

### ⚠️ Tratamento Global de Erros
Todos os erros de regra de negócio retornam JSON padronizado:

{
  "timestamp": "...",
  "status": 400,
  "error": "Erro de Regra de Negócio",
  "message": "Mensagem clara para o App"
}
