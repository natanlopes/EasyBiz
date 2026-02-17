# EasyBiz API - Contrato Completo

> **Version:** 1.1.0
> **Last Update:** 2026-02-15

## Base URL

```
http://localhost:8080
```

Todos os endpoints protegidos exigem o header:

```
Authorization: Bearer {JWT}
```

---

## 1. Autenticacao

### POST /auth/login

Autentica e retorna token JWT.

**Auth:** Nao

**Request:**
```json
{
  "email": "usuario@dominio.com",
  "senha": "123456"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Error 401:** Credenciais invalidas

---

### POST /auth/esqueci-senha

Solicita recuperacao de senha. Envia codigo de 6 digitos por email.

**Auth:** Nao

**Request:**
```json
{
  "email": "usuario@dominio.com"
}
```

**Response 200:**
```json
{
  "mensagem": "Se o email estiver cadastrado, enviaremos um codigo de recuperacao."
}
```

> **Seguranca:** Sempre retorna 200, independente de o email existir ou nao, para nao revelar quais emails estao cadastrados.

---

### POST /auth/redefinir-senha

Redefine a senha usando o codigo recebido por email.

**Auth:** Nao

**Request:**
```json
{
  "token": "482917",
  "novaSenha": "minhaNovaSenha123"
}
```

**Response 200:**
```json
{
  "mensagem": "Senha redefinida com sucesso."
}
```

**Error 400:** Codigo invalido, expirado ou ja utilizado.

**Validacao:**
- `token`: obrigatorio (codigo de 6 digitos)
- `novaSenha`: obrigatorio, minimo 6 caracteres

---

## 2. Usuarios

### POST /usuarios

Cadastra um novo usuario.

**Auth:** Nao

**Request:**
```json
{
  "nomeCompleto": "Joao Silva",
  "email": "joao@email.com",
  "senha": "123456"
}
```

**Response 200:**
```json
{
  "id": 1,
  "nome": "Joao Silva",
  "email": "joao@email.com",
  "fotoUrl": null
}
```

**Error 400:** Email ja cadastrado

**Validacao:**
- `nomeCompleto`: obrigatorio
- `email`: obrigatorio, formato email valido
- `senha`: obrigatorio

---

### GET /usuarios/me

Retorna os dados do usuario autenticado.

**Auth:** Sim

**Response 200:**
```json
{
  "id": 1,
  "nome": "Joao Silva",
  "email": "joao@email.com",
  "fotoUrl": "https://example.com/foto.jpg"
}
```

---

### GET /usuarios/{id}

Retorna o perfil publico de um usuario.

**Auth:** Sim

**Response 200:**
```json
{
  "id": 1,
  "nome": "Joao Silva",
  "email": "joao@email.com",
  "fotoUrl": "https://example.com/foto.jpg"
}
```

**Error 404:** Usuario nao encontrado

---

### PATCH /usuarios/me/foto

Atualiza a foto de perfil do usuario autenticado.

**Auth:** Sim

**Request:**
```json
{
  "url": "https://example.com/nova-foto.jpg"
}
```

**Response:** 204 No Content

**Validacao:**
- `url`: obrigatorio, formato URL valido

---

## 3. Negocios

### POST /negocios

Cria um negocio vinculado ao usuario autenticado.

**Auth:** Sim

**Request:**
```json
{
  "nome": "Barbearia do Joao",
  "categoria": "BARBEARIA"
}
```

**Response 200:**
```json
{
  "id": 1,
  "nome": "Barbearia do Joao",
  "categoria": "BARBEARIA",
  "usuarioId": 1,
  "nomeUsuario": "Joao Silva",
  "ativo": true,
  "latitude": null,
  "longitude": null,
  "enderecoCompleto": null,
  "notaMedia": 0.0,
  "logoUrl": null
}
```

**Validacao:**
- `nome`: obrigatorio
- `categoria`: obrigatorio

**Categorias sugeridas (nao restritas por enum no backend):**
```
BARBEARIA, MECANICA, ELETRICISTA, ENCANADOR, PEDREIRO,
PINTOR, PERSONAL_TRAINER, MOTOTAXI, FRETE, LIMPEZA,
JARDINAGEM, COZINHEIRO, COSTUREIRA, MANICURE, MASSAGISTA,
FOTOGRAFO, PROFESSOR_PARTICULAR, VETERINARIO, OUTROS
```

---

### GET /negocios/busca

Busca inteligente por localizacao e ranking.

**Auth:** Nao

**Parametros:**

| Param   | Tipo   | Obrigatorio | Descricao                          |
|---------|--------|-------------|------------------------------------|
| `lat`   | Double | Sim         | Latitude do usuario                |
| `lon`   | Double | Sim         | Longitude do usuario               |
| `busca` | String | Nao         | Termo de busca (nome ou categoria) |

**Response 200:**
```json
[
  {
    "id": 1,
    "nome": "Barbearia do Joao",
    "categoria": "BARBEARIA",
    "usuarioId": 1,
    "nomeUsuario": "Joao Silva",
    "ativo": true,
    "latitude": -23.5505,
    "longitude": -46.6333,
    "enderecoCompleto": "Rua das Flores, 123",
    "notaMedia": 4.5,
    "logoUrl": "https://example.com/logo.jpg"
  }
]
```

> **Funcionamento:** Usa formula de Haversine no PostgreSQL para calcular distancia. Raio padrao de 30km. Resultados ordenados por avaliacao. Negocios sem coordenadas sao excluidos.

---

### PATCH /negocios/{id}/logo

Atualiza o logo do negocio. Requer ser o dono.

**Auth:** Sim (somente o dono)

**Request:**
```json
{
  "url": "https://example.com/novo-logo.jpg"
}
```

**Response:** 204 No Content

**Error 403:** Nao e o dono do negocio

---

## 4. Pedidos de Servico

### POST /pedidos

Cria um pedido de servico (nova sala de negociacao).

**Auth:** Sim

**Request:**
```json
{
  "negocioId": 1,
  "descricao": "Cortar cabelo as 15h"
}
```

**Response 200:**
```json
{
  "id": 1,
  "clienteId": 2,
  "clienteNome": "Maria Santos",
  "negocioId": 1,
  "negocioNome": "Barbearia do Joao",
  "descricao": "Cortar cabelo as 15h",
  "dataDesejada": null,
  "status": "ABERTO",
  "criadoEm": "2026-02-15T14:30:00"
}
```

**Validacao:**
- `negocioId`: obrigatorio
- `descricao`: obrigatorio

---

### GET /pedidos

Lista pedidos do usuario autenticado (paginado).

**Auth:** Sim

**Parametros de paginacao:**

| Param  | Tipo   | Default | Descricao                       |
|--------|--------|---------|---------------------------------|
| `page` | int    | 0       | Numero da pagina                |
| `size` | int    | 20      | Itens por pagina                |
| `sort` | string | -       | Ordenacao (ex: `criadoEm,desc`) |

**Response 200:**
```json
{
  "content": [
    {
      "id": 1,
      "clienteId": 2,
      "clienteNome": "Maria Santos",
      "negocioId": 1,
      "negocioNome": "Barbearia do Joao",
      "descricao": "Cortar cabelo as 15h",
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

> Retorna pedidos onde o usuario e cliente OU dono do negocio.

---

### PATCH /pedidos/{id}/aceitar

Prestador aceita o pedido.

**Auth:** Sim (somente o prestador)

**Response:** 204 No Content

**Error 400:** Pedido nao esta ABERTO
**Error 403:** Nao e o prestador

---

### PATCH /pedidos/{id}/recusar

Prestador recusa o pedido.

**Auth:** Sim (somente o prestador)

**Response:** 204 No Content

---

### PATCH /pedidos/{id}/concluir

Prestador finaliza o pedido.

**Auth:** Sim (somente o prestador)

**Response:** 204 No Content

**Error 400:** Pedido nao esta ACEITO

> Regra: Nao e possivel concluir um pedido que nao foi aceito.

---

### PATCH /pedidos/{id}/cancelar

Cliente cancela o pedido.

**Auth:** Sim (cliente ou prestador)

**Response:** 204 No Content

---

## 5. Avaliacoes

### POST /avaliacoes/pedido/{pedidoId}

Cliente avalia um pedido concluido.

**Auth:** Sim (somente o cliente)

**Pre-requisito:** Pedido com status `CONCLUIDO`.

**Request:**
```json
{
  "nota": 5,
  "comentario": "Excelente profissional!"
}
```

**Response 200:**
```json
{
  "id": 1,
  "nota": 5,
  "comentario": "Excelente profissional!",
  "dataAvaliacao": "2026-02-15T16:00:00",
  "pedidoId": 1,
  "avaliadorNome": "Maria Santos",
  "avaliadoNome": "Joao Silva",
  "negocioId": 1,
  "negocioNome": "Barbearia do Joao"
}
```

**Validacao:**
- `nota`: obrigatorio, inteiro de 1 a 5
- `comentario`: opcional, maximo 500 caracteres

**Regras:**
- Apenas pedidos CONCLUIDOS podem ser avaliados
- Cada pedido so pode ser avaliado uma vez
- Somente o cliente pode avaliar
- A nota media do negocio e recalculada automaticamente

---

## 6. Mensagens (REST)

### POST /pedidos/{pedidoId}/mensagens

Envia mensagem no chat do pedido.

**Auth:** Sim (participante do pedido)

**Request:**
```json
{
  "conteudo": "Ola, gostaria de agendar para sabado!"
}
```

**Response 200:**
```json
{
  "id": 1,
  "pedidoServicoId": 1,
  "remetenteId": 2,
  "remetenteNome": "Maria Santos",
  "conteudo": "Ola, gostaria de agendar para sabado!",
  "enviadoEm": "2026-02-15T14:35:00",
  "lida": false,
  "lidaEm": null,
  "remetenteFotoUrl": "https://example.com/foto.jpg"
}
```

---

### GET /pedidos/{pedidoId}/mensagens

Retorna historico de mensagens do chat.

**Auth:** Sim (participante do pedido)

**Response 200:** Lista de mensagens (mesma estrutura acima)

**Error 403:** Nao e participante do pedido (protecao IDOR)

---

### POST /pedidos/{pedidoId}/mensagens/lidas

Marca mensagens como lidas.

**Auth:** Sim

**Response:** 204 No Content

---

## 7. WebSocket (Tempo Real)

### Conexao

| Propriedade | Valor                                    |
|-------------|------------------------------------------|
| Endpoint    | `ws://localhost:8080/ws-chat`            |
| Protocolo   | STOMP sobre SockJS                       |
| Auth        | `Authorization: Bearer {JWT}` no CONNECT |

### Topicos (Subscribe)

| Topico                                     | Payload                |
|--------------------------------------------|------------------------|
| `/topic/mensagens/{pedidoId}`              | Mensagem em tempo real |
| `/topic/mensagens/{pedidoId}/digitando`    | Indicador de digitacao |
| `/topic/mensagens/{pedidoId}/lida`         | Confirmacao de leitura |
| `/topic/mensagens/{pedidoId}/ultimo-visto` | Ultimo visto           |

### Enviar (Send)

**Enviar mensagem:** `/app/chat/{pedidoId}`
```json
{
  "conteudo": "Mensagem do usuario"
}
```

**Indicador de digitacao:** `/app/chat/{pedidoId}/digitando`
```json
{
  "usuarioId": 7,
  "usuarioNome": "Cliente",
  "digitando": true
}
```

**Marcar como lida:** `/app/chat/{pedidoId}/lida/{mensagemId}`

> O backend ignora campo `usuarioId` no payload - sempre usa o do token JWT.

---

## 8. Tratamento de Erros

Todos os erros retornam a estrutura padronizada:

```json
{
  "timestamp": "2026-02-15T10:30:00",
  "status": 400,
  "error": "Erro de Regra de Negocio",
  "message": "Mensagem legivel para o app"
}
```

### Status codes

| Codigo | Quando                                                       |
|--------|--------------------------------------------------------------|
| 200    | Sucesso com corpo de resposta (GET e POST)                   |
| 201    | Recurso criado                                               |
| 204    | Sucesso sem corpo (No Content,comum em PATCH desta API))     |
| 400    | Validacao ou regra de negocio                                |
| 401    | Token ausente, invalido ou expirado                          |
| 403    | Token valido mas sem permissao                               |
| 404    | Recurso nao encontrado                                       |
| 429    | Rate limit excedido (max 10 req/min nos endpoints sensiveis) |
| 500    | Erro interno do servidor                                     |

---

## 9. Rate Limiting

Endpoints com limite de requisicoes (10 por minuto por IP):
- `POST /auth/login`
- `POST /usuarios`
- `POST /auth/esqueci-senha`
- `POST /auth/redefinir-senha`

**Response 429:**
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Limite de requisicoes excedido. Tente novamente em 1 minuto."
}
```

---

## 10. Regras de Seguranca

- Rota protegida se nao estiver listada como publica -> JWT obrigatorio
- Token deve ser valido e nao expirado
- Acesso a pedido/chat so permitido a participantes
- WebSocket validado no handshake com token
- IDs de usuario extraidos do token, nunca do payload (protecao IDOR)

### Endpoints publicos
- `POST /auth/login`
- `POST /auth/esqueci-senha`
- `POST /auth/redefinir-senha`
- `POST /usuarios`
- `GET /negocios/**`
- `/ws-chat/**`
- `/swagger-ui/**`
- `/actuator/health`

---

## Swagger UI

Documentacao interativa: http://localhost:8080/swagger-ui/index.html