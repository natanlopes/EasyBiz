# EasyBiz Architecture

## 1. Visao Geral
O EasyBiz utiliza uma **Arquitetura em Camadas (Layered Architecture)** baseada no ecossistema Spring Boot 3. O sistema foi projetado para ser **Stateless** (sem sessao no servidor) e altamente escalavel, focado em alta concorrencia para o Chat em Tempo Real.

## 2. Stack Tecnologica
- **Linguagem:** Java 17 (LTS)
- **Framework:** Spring Boot 3.4.2
- **Database:** PostgreSQL 15 (via Docker)
- **ORM:** Spring Data JPA + Hibernate
- **Migrations:** Flyway
- **Real-Time:** WebSocket (STOMP sobre SockJS)
- **Security:** Spring Security + JWT (JJWT 0.12.6) + BCrypt
- **Validacao:** Bean Validation (Jakarta)
- **Email:** Spring Mail (configuravel dev/prod)
- **Monitoramento:** Spring Actuator
- **Documentacao:** SpringDoc OpenAPI 2.7.0 (Swagger)

## 3. Padrao de Chat Hibrido (Hybrid Chat Pattern)
Para garantir performance e persistencia, adotamos uma abordagem hibrida:

| Camada | Tecnologia | Responsabilidade |
| :--- | :--- | :--- |
| **Historico** | REST API (HTTP) | Carregar mensagens antigas ao abrir a tela. Garante que nada seja perdido. |
| **Ao Vivo** | WebSocket (STOMP) | Entrega instantanea de novas mensagens sem *polling*. |

**Decisao de Design:**
Nao usamos o WebSocket para buscar historico para evitar sobrecarga no broker de mensagens. O banco de dados relacional (Postgres) e mais eficiente para consultas de historico via HTTP.

## 4. Modelagem de Dados (ER Simplificado)
- **Usuario:** Entidade base (Cliente ou Prestador).
- **Negocio:** Pertence a um Usuario. Define a "vitrine" do servico.
- **PedidoServico:** A "Sala de Reuniao". Liga um Cliente a um Negocio.
- **Mensagem:** Pertence a um PedidoServico.
- **Avaliacao:** Gerada apenas apos o ciclo de vida do Pedido ser concluido.
- **PasswordResetToken:** Token de recuperacao de senha com expiracao.

## 5. Escalabilidade
O sistema esta preparado para rodar em containers (Docker).
Como a autenticacao e via JWT (Stateless), e possivel subir multiplas instancias da API atras de um Load Balancer sem quebrar a sessao do usuario.

> **Nota V2:** O SimpleBroker do WebSocket e in-memory. Para multiplas instancias, sera necessario um broker externo (Redis/RabbitMQ).

## 6. Busca Inteligente
A busca utiliza calculo de distancia (Haversine) diretamente no banco,
reduzindo carga no backend e garantindo performance.

A nota media do negocio e recalculada a cada nova avaliacao e persistida na entidade Negocio para otimizar buscas e rankings.

Negocios sem coordenadas (latitude/longitude nulos) sao automaticamente excluidos da busca.

---

## 7. Arquitetura de Seguranca

### Camadas de Protecao

```
+-------------------------------------------------------------+
|                      CLIENTE (App/Web)                        |
+---------------------------+---------------------------------+
                            |
                            v
+-------------------------------------------------------------+
|                    RATE LIMITING                               |
|         (10 req/min em /auth/login e POST /usuarios)          |
+---------------------------+---------------------------------+
                            |
           +----------------+----------------+
           |                                 |
           v                                 v
+---------------------+         +---------------------+
|   REST Endpoints    |         |  WebSocket Server   |
| JwtAuthFilter       |         | JwtChannelIntercept |
+---------+-----------+         +---------+-----------+
          |                               |
          +---------------+---------------+
                          |
                          v
+-------------------------------------------------------------+
|                   SPRING SECURITY CONTEXT                     |
|                   (Principal = email do usuario)              |
+---------------------------+---------------------------------+
                            |
                            v
+-------------------------------------------------------------+
|                      SERVICE LAYER                             |
|              (Validacao de Ownership/IDOR)                     |
|              (Excecoes Customizadas)                           |
+---------------------------+---------------------------------+
                            |
                            v
+-------------------------------------------------------------+
|                     REPOSITORY LAYER                           |
|                      (JPA/Hibernate)                           |
+---------------------------+---------------------------------+
                            |
                            v
+-------------------------------------------------------------+
|                      PostgreSQL                                |
|               (Senhas em BCrypt Hash)                          |
|               (Flyway Migrations)                              |
+-------------------------------------------------------------+
```

### Principios de Seguranca

| Principio | Implementacao |
|-----------|--------------|
| **Defense in Depth** | Multiplas camadas de validacao |
| **Least Privilege** | Acesso apenas ao necessario |
| **Secure by Default** | Endpoints bloqueados por padrao |
| **No Secrets in Code** | Variaveis de ambiente |

### Configuracoes Sensiveis

```properties
# application.properties - Usa variaveis de ambiente
spring.datasource.password=${DB_PASSWORD}
api.security.token.secret=${JWT_SECRET}
```

> Detalhes completos em [SECURITY.md](./SECURITY.md)

---

## 8. Global Exception Handling

Camada de `@RestControllerAdvice` para interceptar excecoes e garantir respostas padronizadas.

### Excecoes Customizadas

| Excecao | HTTP Status |
|---------|------------|
| `ResourceNotFoundException` | 404 Not Found |
| `UnauthorizedException` | 401 Unauthorized |
| `ForbiddenException` | 403 Forbidden |
| `BusinessException` | 400 Bad Request |

### Formato Padrao (ApiError)

```json
{
  "timestamp": "2026-02-15T10:00:00",
  "status": 400,
  "error": "Erro de Regra de Negocio",
  "message": "Mensagem clara para o App"
}
```

Beneficios:
- Sem vazamento de stack traces
- Backend previsivel
- Mensagens amigaveis para o frontend

---

## 9. Database Migrations (Flyway)

O projeto usa **Flyway** para versionamento do schema do banco de dados.

| Migration | Descricao |
|-----------|-----------|
| `V1__baseline.sql` | Schema inicial: usuarios, negocios, pedido_servico, mensagem, avaliacao |
| `V2__add_password_reset_tokens.sql` | Tabela de tokens de recuperacao de senha |

### Configuracao

```properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0

# Em producao: ddl-auto=validate (Flyway gerencia o schema)
spring.jpa.hibernate.ddl-auto=validate
```

> Em testes: usa H2 in-memory com `create-drop` e Flyway desabilitado.

---

## 10. Email Service

O projeto inclui servico de email configuravel:

| Ambiente | Comportamento |
|----------|---------------|
| **Desenvolvimento** | `app.mail.enabled=false` - Loga no console |
| **Producao** | `app.mail.enabled=true` - Envia via SMTP |

Usado para:
- Recuperacao de senha (codigo de 6 digitos)

---

## 11. Monitoramento (Actuator)

Spring Boot Actuator configurado para health checks:

```properties
# application-prod.properties
management.endpoints.web.exposure.include=health,info
```

Usado pelo Railway para verificar saude da aplicacao (`/actuator/health`).

---

## 12. Estrutura de Pastas

```
src/main/java/br/com/easybiz/
+-- config/           # SecurityConfig, WebSocketConfig, OpenAPIConfig
+-- controller/       # REST Controllers (7)
+-- dto/              # Data Transfer Objects - Records (17)
+-- exception/        # Excecoes customizadas + GlobalExceptionHandler
+-- model/            # Entidades JPA (7)
+-- repository/       # Spring Data JPA Repositories (7)
+-- security/         # JwtService, JwtFilter, RateLimitFilter, Interceptors
+-- service/          # Regras de negocio (8)

src/main/resources/
+-- db/migration/     # Flyway migrations (SQL)
+-- application.properties
+-- application-local.properties
+-- application-prod.properties

docs/
+-- API.md            # Contrato da API
+-- ARCHITECTURE.md   # Este arquivo
+-- SECURITY.md       # Documentacao de seguranca
+-- WORKFLOW.md       # Fluxos e estados
+-- INTEGRATION.md    # Guia de integracao para frontend
+-- CHANGELOG.md      # Historico de mudancas
```