# Changelog

Todas as mudancas notaveis do projeto EasyBiz serao documentadas neste arquivo.

O formato e baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---
## [1.1.1] - 2026-02-17
- Corrigidos contratos HTTP em `API.md` e `INTEGRATION.md` para refletir status reais dos endpoints (`/auth/login` = 200, `/usuarios` = 201, `/negocios` = 201).
- Corrigida a resposta de `GET /usuarios/{id}` na documentação (perfil público sem e-ma
## [1.1.0] - 2026-02-15

### Seguranca (P0)
- **Dockerfile**: Corrigido Java 21 -> Java 17 (alinhado com pom.xml)
- **SecurityConfig**: Corrigido `POST /usuarios/**` -> `POST /usuarios` (sem wildcard)
- **GlobalExceptionHandler**: Handler dedicado para `SecurityException` retornando 403 Forbidden
- **application-local.properties**: JWT secret agora usa `${JWT_SECRET:fallback}`
- **docker-compose.yaml**: Senha usa `${POSTGRES_PASSWORD:-admin}` com .env
- **Testes**: Reconfigurado para H2 in-memory com `create-drop`

### Adicionado
- **Recuperacao de senha**: `POST /auth/esqueci-senha` e `POST /auth/redefinir-senha`
  - Codigo de 6 digitos via email
  - Expiracao em 15 minutos
  - Single-use (token marcado como utilizado)
  - Seguranca: nao revela se email existe
- **PasswordResetService**: Servico de reset com `SecureRandom`
- **EmailService**: Servico de email configuravel (dev: console, prod: SMTP)
- **PasswordResetToken**: Entidade + migration V2
- **RateLimitFilter**: 10 req/min em `/auth/login` e `POST /usuarios`
- **Excecoes customizadas**: `ResourceNotFoundException`, `UnauthorizedException`, `ForbiddenException`, `BusinessException`
- **ApiError**: DTO padronizado para respostas de erro
- **Flyway**: Migrations versionadas (V1 baseline + V2 password reset)
- **Spring Actuator**: Health check em `/actuator/health`
- **Railway**: Configuracao de deploy via `railway.toml`
- **Paginacao**: `GET /pedidos` agora retorna `Page<PedidoServicoResponseDTO>` (20 items/page)
- **NegocioResponseDTO**: Todos os endpoints de negocio retornam DTO (nao entidade)
- **GET /usuarios/{id}**: Endpoint de perfil publico
- **PATCH /usuarios/me/foto**: Atualizacao de foto de perfil
- **AtualizarFotoDTO**: Com validacao `@NotBlank` e `@URL`

### Corrigido (P1)
- **NegocioService**: Removido conflito `@RequiredArgsConstructor` + construtor manual
- **Entidades**: Removidos ~300 linhas de getters/setters manuais duplicados (Lombok)
- **Avaliacao**: `dataAvaliacao` movido para `@PrePersist` (timestamp correto)
- **Avaliacao**: Adicionado `@Table(name = "avaliacao")`, trocado `@Data` por `@Getter/@Setter`
- **DTOs**: Removidos metodos acessores redundantes de records
- **CriarNegocioDTO**: Removido metodo `tipo()` confuso
- **PedidoServicoController**: Substituido `UsuarioRepository` por `AuthContextService`
- **NegocioRepository**: Query filtra `latitude IS NOT NULL AND longitude IS NOT NULL`
- **Login**: Retorna 401 (nao 400) para credenciais invalidas

### Corrigido (P2)
- **DTOs**: Adicionadas validacoes Bean Validation em todos os DTOs
- **CORS**: Configurado via property `app.cors.allowed-origins` (nao hardcoded)
- **JJWT**: Atualizado de 0.11.5 para 0.12.6
- **Logging**: Security DEBUG apenas no perfil de teste

### Documentacao
- **API.md**: Reescrito com todos os endpoints corretos
- **INTEGRATION.md**: Corrigidos paths errados, adicionado password recovery, DTOs atualizados
- **SECURITY.md**: Adicionado rate limiting, recuperacao de senha, excecoes customizadas
- **ARCHITECTURE.md**: Adicionado Flyway, Spring Mail, Actuator
- **WORKFLOW.md**: Diagrama corrigido com CANCELADO separado de RECUSADO
- **CHANGELOG.md**: Historico completo de mudancas

### Removido
- **teste-chat.html**: Arquivo de teste removido do root

### Arquivos Modificados

```
src/main/java/br/com/easybiz/
+-- controller/
|   +-- AuthController.java             (+ esqueci-senha, redefinir-senha)
|   +-- UsuarioController.java          (+ GET /{id}, PATCH /me/foto)
|   +-- NegocioController.java          (retorna DTO)
|   +-- PedidoServicoController.java    (paginacao, AuthContextService)
+-- dto/
|   +-- EsqueciSenhaRequestDTO.java     NOVO
|   +-- RedefinirSenhaRequestDTO.java   NOVO
|   +-- AtualizarFotoDTO.java           NOVO
|   +-- NegocioResponseDTO.java         NOVO
|   +-- PedidoServicoResponseDTO.java   NOVO
|   +-- MensagemResponseDTO.java        NOVO
+-- exception/
|   +-- GlobalExceptionHandler.java     (handler SecurityException, ApiError)
|   +-- ResourceNotFoundException.java  NOVO
|   +-- UnauthorizedException.java      NOVO
|   +-- ForbiddenException.java         NOVO
|   +-- BusinessException.java          NOVO
|   +-- ApiError.java                   NOVO
+-- model/
|   +-- PasswordResetToken.java         NOVO
|   +-- Avaliacao.java                  (@PrePersist, @Table)
+-- security/
|   +-- RateLimitFilter.java            NOVO
+-- service/
|   +-- PasswordResetService.java       NOVO
|   +-- EmailService.java               NOVO

src/main/resources/
+-- db/migration/
|   +-- V1__baseline.sql                NOVO
|   +-- V2__add_password_reset_tokens.sql NOVO

Dockerfile                              (Java 21 -> 17)
docker-compose.yaml                     (env vars)
railway.toml                            NOVO
.env.example                            (documentado)

docs/
+-- API.md                              ATUALIZADO
+-- ARCHITECTURE.md                     ATUALIZADO
+-- SECURITY.md                         ATUALIZADO
+-- WORKFLOW.md                         ATUALIZADO
+-- INTEGRATION.md                      ATUALIZADO
+-- CHANGELOG.md                        ATUALIZADO
```

---

## [1.0.1] - 2025-02-05

### Seguranca
- **JwtService**: Removido secret hardcoded, agora usa variavel de ambiente `${JWT_SECRET}`
- **SecurityConfig**: Removido `.permitAll()` temporario das rotas `/negocios/**` e `/pedidos/**`

### Adicionado
- **AvaliacaoResponseDTO**: Novo DTO para retorno seguro de avaliacoes
- **StatusPedido.CANCELADO**: Novo status no enum para pedidos cancelados
- **PedidoServicoService.cancelar()**: Metodo para cliente cancelar pedidos

### Documentacao
- **AvaliacaoResponseDTO**: Documentacao completa com `@Schema` (Swagger/OpenAPI)
- **AvaliacaoController**: Anotacoes `@Operation`, `@ApiResponse`, `@Parameter` completas
- **SECURITY.md**: Atualizado com BCrypt, variaveis de ambiente e checklist
- **ARCHITECTURE.md**: Adicionada secao de arquitetura de seguranca

### Alterado
- **AvaliacaoService.avaliarPedido()**: Agora retorna `AvaliacaoResponseDTO`
- **AvaliacaoController.avaliar()**: Atualizado para usar o novo DTO de resposta

---

## [1.0.0] - 2025-01-30

### Release Inicial - MVP V1

#### Funcionalidades Core
- **Autenticacao**: JWT com Spring Security
- **Usuarios**: Cadastro, login, perfil
- **Negocios**: CRUD completo, busca por geolocalizacao (Haversine)
- **Pedidos**: Workflow completo (ABERTO -> ACEITO -> CONCLUIDO)
- **Chat**: WebSocket STOMP em tempo real + historico REST
- **Avaliacoes**: Sistema de notas com atualizacao de média

#### Documentacao
- API.md: Contrato completo da API
- ARCHITECTURE.md: Visao tecnica do sistema
- SECURITY.md: Documentacao de seguranca
- WORKFLOW.md: Fluxos e estados

#### Infraestrutura
- Docker Compose para PostgreSQL
- Swagger UI para documentacao interativa
- Global Exception Handler

---

## Convencao de Commits

| Tipo              | Descricao                           |
|-------------------|-------------------------------------|
| `feat(EB-XX)`     | Nova funcionalidade                 |
| `fix(EB-XX)`      | Correcao de bug                     |
| `docs(EB-XX)`     | Apenas documentacao                 |
| `infra(EB-XX)`    | Infraestrutura/deploy               |
| `refactor(EB-XX)` | Refatoracao sem mudar comportamento |
| `test(EB-XX)`     | Adicionar/corrigir testes           |

## Versionamento

- **MAJOR** (1.x.x): Mudancas incompativeis na API
- **MINOR** (x.1.x): Novas funcionalidades retrocompativeis
- **PATCH** (x.x.1): Correcoes de bugs retrocompativeis