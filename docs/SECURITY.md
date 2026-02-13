# 🔐 Security & Authentication

> Documentação de segurança do EasyBiz - Atualizado em 2025-02-05

---

## 1. Visão Geral de Segurança

| Camada | Tecnologia | Status |
|--------|-----------|--------|
| Autenticação | JWT (HS256) | ✅ Implementado |
| Hash de Senhas | BCrypt | ✅ Implementado |
| Proteção REST | JwtAuthenticationFilter | ✅ Implementado |
| Proteção WebSocket | WebSocketJwtInterceptor | ✅ Implementado |
| Configurações Sensíveis | Variáveis de Ambiente | ✅ Implementado |

---

## 2. 🔑 Autenticação JWT

### Configuração
- **Algoritmo:** HS256 (HMAC-SHA256)
- **Secret:** Mínimo 32 caracteres (via variável de ambiente)
- **Validade:** 24 horas (86400000ms)
- **Formato:** `Authorization: Bearer <token>`

### Fluxo de Autenticação

```
┌─────────┐     POST /auth/login      ┌─────────┐
│ Cliente │ ──────────────────────────▶│ Backend │
│  (App)  │   {email, senha}          │  (API)  │
└─────────┘                           └────┬────┘
                                           │
                                           ▼
                                    ┌─────────────┐
                                    │ 1. Busca    │
                                    │    Usuário  │
                                    └──────┬──────┘
                                           │
                                           ▼
                                    ┌─────────────┐
                                    │ 2. Valida   │
                                    │   BCrypt    │
                                    └──────┬──────┘
                                           │
                                           ▼
                                    ┌─────────────┐
                                    │ 3. Gera JWT │
                                    └──────┬──────┘
                                           │
┌─────────┐      {token: "eyJ..."}   ◀─────┘
│ Cliente │ ◀────────────────────────
└─────────┘
```

### Estrutura do Token

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "123",        // ID do usuário
    "iat": 1707148800,   // Issued at
    "exp": 1707235200    // Expiration (24h)
  }
}
```

---

## 3. 🔒 Hash de Senhas (BCrypt)

### Implementação
O sistema utiliza **BCrypt** para hash de senhas, que é o padrão da indústria.

**Por que BCrypt?**
- ✅ Resistente a ataques de força bruta (work factor ajustável)
- ✅ Inclui salt automaticamente
- ✅ Impossível reverter para a senha original
- ✅ Padrão recomendado pelo OWASP

### Código de Referência

```
java
// SecurityConfig.java - Bean do encoder
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // Work factor padrão: 10
}

// UsuarioService.java - Ao criar usuário
usuario.setSenha(passwordEncoder.encode(dto.senha()));

// AuthController.java - Ao fazer login
if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
    throw new RuntimeException("Credenciais inválidas");
}
```

### Exemplo de Hash

```
Senha original: "123456"
Hash BCrypt:    "$2a$10$N9qo8uLOickgx2ZMRZoMy..."

⚠️ Cada hash é único mesmo para a mesma senha (por causa do salt)
```

---

## 4. 🛡️ Proteção de Endpoints

### REST API (JwtAuthenticationFilter)

Todas as requisições HTTP passam pelo filtro JWT:

1. Extrai o header `Authorization: Bearer <token>`
2. Valida assinatura e expiração do token
3. Extrai o ID do usuário (Subject)
4. Injeta no contexto do Spring Security

### Endpoints Públicos (Sem autenticação)

| Endpoint | Método | Descrição |
|----------|--------|-----------|
| `/auth/**` | POST | Login |
| `/usuarios` | POST | Cadastro |
| `/ws-chat/**` | WS | WebSocket handshake |
| `/swagger-ui/**` | GET | Documentação |
| `/v3/api-docs/**` | GET | OpenAPI spec |

### Endpoints Protegidos 🔒

Todos os outros endpoints requerem JWT válido.

---

## 5. 🌐 Proteção WebSocket

### Desafio
O protocolo WebSocket não suporta headers nativos no handshake do navegador.

### Solução: WebSocketJwtInterceptor

```
┌─────────┐    CONNECT + token     ┌─────────────────┐
│ Cliente │ ──────────────────────▶│ WebSocket       │
└─────────┘                        │ Interceptor     │
                                   └────────┬────────┘
                                            │
                                   ┌────────▼────────┐
                                   │ Valida JWT      │
                                   └────────┬────────┘
                                            │
                            ┌───────────────┴───────────────┐
                            │                               │
                     ┌──────▼──────┐               ┌───────▼───────┐
                     │ Token Válido│               │ Token Inválido│
                     │ → Conecta   │               │ → Rejeita     │
                     └─────────────┘               └───────────────┘
```

### Fluxo
1. Intercepta evento `CONNECT` do STOMP
2. Lê header nativo `Authorization`
3. Valida Token JWT
4. **Se inválido:** Rejeita conexão
5. **Se válido:** Injeta Principal (email do usuário) na sessão

---

## 6. 🚫 Prevenção de Spoofing

### Problema
Um usuário malicioso poderia tentar enviar mensagens em nome de outro alterando o payload JSON.

### Solução
O sistema **ignora** qualquer ID de usuário enviado no corpo do JSON.

**Regra:** O remetente é SEMPRE extraído do `Principal` (Token JWT), nunca do payload.

```
java
// ❌ NUNCA confiar no payload
Long remetenteId = dto.getUsuarioId(); // IGNORADO

// ✅ SEMPRE usar o Principal do token
String remetenteEmail = principal.getName();
Long remetenteId = authContextService.getUsuarioIdByEmail(remetenteEmail);


```

---

## 7. 🔐 Gerenciamento de Secrets

### ⚠️ REGRAS CRÍTICAS

| ❌ NUNCA | ✅ SEMPRE |
|----------|-----------|
| Hardcoded no código | Variáveis de ambiente |
| Commitar no Git | Usar `.env` local |
| Compartilhar em chat | Usar secrets manager |

### Configuração

**application.properties:**
```properties
# Usa variável de ambiente - NUNCA hardcode!
spring.datasource.password=${DB_PASSWORD}
api.security.token.secret=${JWT_SECRET}
```

**.env (local - NÃO commitar):**
```bash
DB_PASSWORD=sua_senha_segura_aqui
JWT_SECRET=chave_com_pelo_menos_32_caracteres_para_hs256
```

**.gitignore:**
```gitignore
.env
*.env
!.env.example
application-local.properties
```

### Gerar JWT Secret Seguro

```bash
# Linux/Mac
openssl rand -base64 32

# PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# Exemplo de output:
# K7gNU3sdo+OL0wNhqoVWhr3g6s1xYv72ol/pe/Unols=
```

---

## 8. 🛡️ Isolamento de Dados (IDOR Protection)

### Regras de Acesso

| Recurso | Quem pode acessar |
|---------|-------------------|
| Pedido | Cliente OU Dono do Negócio |
| Mensagens | Participantes do pedido |
| Negócio (editar) | Apenas o dono |
| Avaliação | Apenas o cliente (criar) |

### Validação em Código

```java
// Verifica se o usuário tem acesso ao pedido
if (!pedido.getCliente().getId().equals(usuarioId) && 
    !pedido.getNegocio().getUsuario().getId().equals(usuarioId)) {
    throw new SecurityException("Acesso negado");
}
```

### Respostas de Erro

| Código | Quando |
|--------|--------|
| 401 Unauthorized | Token ausente ou inválido |
| 403 Forbidden | Token válido, mas sem permissão |
| 404 Not Found | Recurso não existe (ou oculto por segurança) |

---

## 9. 📋 Checklist de Segurança

### Antes de Deploy

- [ ] JWT_SECRET definido via variável de ambiente
- [ ] DB_PASSWORD definido via variável de ambiente
- [ ] `.env` no `.gitignore`
- [ ] application-local.properties no `.gitignore`
- [ ] HTTPS habilitado em produção
- [ ] CORS configurado corretamente
- [ ] Rate limiting configurado
- [ ] Logs não expõem dados sensíveis

### Auditoria Periódica

- [ ] Rotacionar JWT_SECRET a cada 90 dias
- [ ] Revisar permissões de endpoints
- [ ] Verificar dependências com vulnerabilidades (OWASP)
- [ ] Testar proteção contra IDOR
- [ ] Validar expiração de tokens

---

## 10. 🚨 Resposta a Incidentes

### Se credenciais vazarem:

1. **Imediatamente:** Rotacionar todas as credenciais afetadas
2. **Investigar:** Verificar logs de acesso anormal
3. **Notificar:** Informar usuários se dados foram expostos
4. **Prevenir:** Revisar e melhorar controles

### Contatos

- **Security Lead:** security@easybiz.com
- **DevOps:** devops@easybiz.com

---

## Histórico de Atualizações

| Data | Versão | Mudança |
|------|--------|---------|
| 2025-02-05 | 1.1 | Adicionado BCrypt, variáveis de ambiente, checklist |
| 2025-01-30 | 1.0 | Documentação inicial JWT e WebSocket |
