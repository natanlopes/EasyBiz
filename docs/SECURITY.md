# 🔐 Security & Authentication

## 1. Estratégia de Autenticação
O sistema utiliza **JWT (JSON Web Tokens)** assinado com algoritmo **HS256**.
- **Chave:** 256-bit secret key.
- **Validade:** 24 horas.
- **Formato:** Header `Authorization: Bearer <token>`.

## 2. Camadas de Proteção

### A. Proteção REST (Http Filter)
Todas as requisições HTTP passam pelo `JwtAuthenticationFilter`.
- Verifica assinatura do token.
- Extrai o ID do usuário (Subject).
- Monta o `UsernamePasswordAuthenticationToken` no contexto do Spring.

### B. Proteção WebSocket (Channel Interceptor)
O protocolo WebSocket não suporta headers nativos no handshake padrão do navegador.
**Solução Implementada:** `WebSocketJwtInterceptor`.
1. Intercepta o evento `CONNECT` do protocolo STOMP.
2. Lê o header nativo `Authorization`.
3. Valida o Token JWT.
4. **Se inválido:** Rejeita a conexão imediatamente.
5. **Se válido:** Injeta o `Principal` (User ID) na sessão do Socket.

## 3. Prevenção de Spoofing (Identidade Falsa)
O sistema **ignora** qualquer ID de usuário enviado no corpo do JSON (Payload) para fins de identificação de remetente.

**Regra:** O remetente da mensagem é sempre extraído do `Principal` (Token), garantindo que um usuário nunca possa enviar mensagens em nome de outro, mesmo que altere o JavaScript no Front-end.

## 4. Isolamento de Dados
- Um usuário só pode ler mensagens de um pedido se for o **Cliente** ou o **Dono do Negócio** daquele pedido.
- Tentativas de acesso a pedidos alheios resultam em `403 Forbidden` ou `SecurityException`.

---
## 🆕 Proteções Implementadas

- Uso obrigatório de JWT para ações sensíveis
- Validação de identidade via `Principal`
- Proteção contra IDOR em:
  - Listagem de pedidos
  - Ações de aceitar/recusar/concluir
- DTOs de resposta evitam vazamento de dados sensíveis
