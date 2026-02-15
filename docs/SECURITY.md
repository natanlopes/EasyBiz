# Security & Authentication

> Documentacao de seguranca do EasyBiz - Atualizado em 2026-02-15

---

## 1. Visao Geral de Seguranca

| Camada | Tecnologia | Status |
|--------|-----------|--------|
| Autenticacao | JWT (HS256) | Implementado |
| Hash de Senhas | BCrypt | Implementado |
| Protecao REST | JwtAuthenticationFilter | Implementado |
| Protecao WebSocket | WebSocketJwtInterceptor | Implementado |
| Rate Limiting | RateLimitFilter (10 req/min) | Implementado |
| Recuperacao de Senha | Token 6 digitos + Email | Implementado |
| Excecoes Customizadas | GlobalExceptionHandler | Implementado |
| Configuracoes Sensiveis | Variaveis de Ambiente | Implementado |

---

## 2. Autenticacao JWT

### Configuracao
- **Algoritmo:** HS256 (HMAC-SHA256)
- **Secret:** Minimo 32 caracteres (via variavel de ambiente `JWT_SECRET`)
- **Validade:** 24 horas (86400000ms)
- **Formato:** `Authorization: Bearer <token>`
- **Biblioteca:** JJWT 0.12.6

### Fluxo de Autenticacao

```
[Cliente (App)]     POST /auth/login      [Backend (API)]
       |            {email, senha}               |
       |---------------------------------------->|
       |                                         |
       |                                  1. Busca Usuario
       |                                  2. Valida BCrypt
       |                                  3. Gera JWT
       |                                         |
       |            {token: "eyJ..."}            |
       |<----------------------------------------|
```

### Estrutura do Token

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "usuario@email.com",
    "iat": 1707148800,
    "exp": 1707235200
  }
}
```

---

## 3. Hash de Senhas (BCrypt)

### Implementacao
O sistema utiliza **BCrypt** para hash de senhas, padrao recomendado pelo OWASP.

- Resistente a ataques de forca bruta (work factor ajustavel)
- Inclui salt automaticamente
- Impossivel reverter para a senha original
- Work factor padrao: 10

### Codigo de Referencia

```java
// SecurityConfig.java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// UsuarioService.java - Ao criar usuario
usuario.setSenha(passwordEncoder.encode(dto.senha()));

// AuthController.java - Ao fazer login
if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
    throw new UnauthorizedException("Credenciais invalidas");
}
```

---

## 4. Protecao de Endpoints

### REST API (JwtAuthenticationFilter)

Todas as requisicoes HTTP passam pelo filtro JWT:

1. Extrai o header `Authorization: Bearer <token>`
2. Valida assinatura e expiracao do token
3. Extrai o email do usuario (Subject)
4. Injeta no contexto do Spring Security

### Endpoints Publicos (Sem autenticacao)

| Endpoint | Metodo | Descricao |
|----------|--------|-----------|
| `/auth/login` | POST | Login |
| `/auth/esqueci-senha` | POST | Solicitar recuperacao de senha |
| `/auth/redefinir-senha` | POST | Redefinir senha com codigo |
| `/usuarios` | POST | Cadastro |
| `/negocios/**` | GET | Busca de negocios |
| `/ws-chat/**` | WS | WebSocket handshake |
| `/swagger-ui/**` | GET | Documentacao |
| `/v3/api-docs/**` | GET | OpenAPI spec |
| `/actuator/**` | GET | Health check e metricas |

### Endpoints Protegidos

Todos os outros endpoints requerem JWT valido.

---

## 5. Rate Limiting

### RateLimitFilter

Protecao contra brute force em endpoints sensiveis:

| Endpoint | Limite | Janela |
|----------|--------|--------|
| `POST /auth/login` | 10 requisicoes | 1 minuto |
| `POST /usuarios` | 10 requisicoes | 1 minuto |

**Implementacao:**
- Identificacao por IP (`X-Forwarded-For` ou `remoteAddr`)
- Janela deslizante (sliding window) in-memory
- Response `429 Too Many Requests` quando excedido
- Chave: `IP:path` (cada endpoint tem seu proprio contador)

---

## 6. Recuperacao de Senha

### Fluxo de Seguranca

```
1. Usuario solicita reset (POST /auth/esqueci-senha)
2. Backend gera codigo de 6 digitos (SecureRandom)
3. Codigos anteriores nao utilizados sao invalidados
4. Codigo e salvo com expiracao de 15 minutos
5. Email enviado com o codigo
6. API retorna 200 (independente se email existe - seguranca)
7. Usuario envia codigo + nova senha (POST /auth/redefinir-senha)
8. Backend valida: codigo existe, nao expirou, nao foi usado
9. Senha atualizada com BCrypt
10. Token marcado como utilizado (single-use)
```

### Protecoes
- **Enumeracao de emails:** API sempre retorna 200 no esqueci-senha
- **Brute force:** Rate limiting no endpoint
- **Token unico:** Cada solicitacao invalida tokens anteriores
- **Expiracao:** Token expira em 15 minutos
- **Single-use:** Token so pode ser usado uma vez

---

## 7. Protecao WebSocket

### WebSocketJwtInterceptor

```
[Cliente]    CONNECT + token     [WebSocket Interceptor]
    |                                    |
    |----------------------------------->|
    |                             Valida JWT
    |                                    |
    |                    +---------------+---------------+
    |                    |                               |
    |             Token Valido                    Token Invalido
    |             -> Conecta                      -> Rejeita
```

### Fluxo
1. Intercepta evento `CONNECT` do STOMP
2. Le header nativo `Authorization`
3. Valida Token JWT
4. **Se invalido:** Rejeita conexao
5. **Se valido:** Injeta Principal (email do usuario) na sessao

---

## 8. Prevencao de Spoofing (IDOR Protection)

### Problema
Um usuario malicioso poderia tentar enviar mensagens em nome de outro alterando o payload JSON.

### Solucao
O sistema **ignora** qualquer ID de usuario enviado no corpo do JSON.

**Regra:** O remetente e SEMPRE extraido do `Principal` (Token JWT), nunca do payload.

```java
// NUNCA confiar no payload
Long remetenteId = dto.getUsuarioId(); // IGNORADO

// SEMPRE usar o Principal do token
String remetenteEmail = principal.getName();
Long remetenteId = authContextService.getUsuarioIdByEmail(remetenteEmail);
```

### Regras de Acesso

| Recurso | Quem pode acessar |
|---------|-------------------|
| Pedido | Cliente OU Dono do Negocio |
| Mensagens | Participantes do pedido |
| Negocio (editar) | Apenas o dono |
| Avaliacao | Apenas o cliente (criar) |

---

## 9. Excecoes Customizadas

### Hierarquia

| Excecao | HTTP Status | Quando |
|---------|------------|--------|
| `ResourceNotFoundException` | 404 | Recurso nao encontrado |
| `UnauthorizedException` | 401 | Credenciais invalidas, token invalido |
| `ForbiddenException` | 403 | Sem permissao para a acao |
| `BusinessException` | 400 | Violacao de regra de negocio |
| `SecurityException` | 403 | Acesso negado (seguranca) |
| `IllegalStateException` | 400 | Estado invalido |
| `MethodArgumentNotValidException` | 400 | Validacao de campos (Bean Validation) |

### Formato de Resposta (ApiError)

```json
{
  "timestamp": "2026-02-15T10:00:00",
  "status": 400,
  "error": "Erro de Regra de Negocio",
  "message": "Pedido ja foi avaliado"
}
```

O `GlobalExceptionHandler` garante que stack traces nunca sao expostas ao cliente.

---

## 10. Gerenciamento de Secrets

### Regras

| Nunca | Sempre |
|-------|--------|
| Hardcoded no codigo | Variaveis de ambiente |
| Commitar no Git | Usar `.env` local |
| Compartilhar em chat | Usar secrets manager |

### Configuracao

**application.properties:**
```properties
spring.datasource.password=${DB_PASSWORD}
api.security.token.secret=${JWT_SECRET}
```

**.env (local - NAO commitar):**
```bash
DB_PASSWORD=sua_senha_segura_aqui
JWT_SECRET=chave_com_pelo_menos_32_caracteres_para_hs256
```

### Gerar JWT Secret Seguro

```bash
# Linux/Mac
openssl rand -base64 32

# PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

---

## 11. Checklist de Seguranca

### Antes de Deploy

- [x] JWT_SECRET definido via variavel de ambiente
- [x] DB_PASSWORD definido via variavel de ambiente
- [x] `.env` no `.gitignore`
- [x] application-local.properties no `.gitignore`
- [x] CORS configurado via property (`app.cors.allowed-origins`)
- [x] Rate limiting configurado (RateLimitFilter)
- [x] Excecoes customizadas com HTTP status corretos
- [x] Protecao IDOR em todos os endpoints
- [ ] HTTPS habilitado em producao
- [ ] Logs nao expoem dados sensiveis (verificar em prod)

### Auditoria Periodica

- [ ] Rotacionar JWT_SECRET a cada 90 dias
- [ ] Revisar permissoes de endpoints
- [ ] Verificar dependencias com vulnerabilidades (OWASP)
- [ ] Testar protecao contra IDOR
- [ ] Validar expiracao de tokens

---

## 12. Resposta a Incidentes

### Se credenciais vazarem:

1. **Imediatamente:** Rotacionar todas as credenciais afetadas
2. **Investigar:** Verificar logs de acesso anormal
3. **Notificar:** Informar usuarios se dados foram expostos
4. **Prevenir:** Revisar e melhorar controles

---

## Historico de Atualizacoes

| Data | Versao | Mudanca |
|------|--------|---------|
| 2026-02-15 | 1.2 | Adicionado rate limiting, recuperacao de senha, excecoes customizadas |
| 2025-02-05 | 1.1 | Adicionado BCrypt, variaveis de ambiente, checklist |
| 2025-01-30 | 1.0 | Documentacao inicial JWT e WebSocket |