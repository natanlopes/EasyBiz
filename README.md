# EasyBiz

## Visao Geral

O **EasyBiz** e uma plataforma digital inspirada em modelos como iFood, porem voltada para **qualquer tipo de servico**.

A proposta e simples: permitir que **qualquer pessoa**, mesmo sem conhecimento tecnico, consiga **divulgar seus servicos**, ser encontrada por clientes da sua regiao e **negociar atendimentos de forma flexivel**.

O foco e **disponibilidade flexivel e negociacao direta**, refletindo a realidade de profissionais como:

* Pedreiros
* Mecanicos
* Barbeiros
* Personal trainers
* Mototaxis
* Prestadores de servicos em geral

---

## Problema que o EasyBiz resolve

Hoje, muitos profissionais:

* Nao sabem usar sistemas complexos de agenda
* Trabalham com horarios variaveis
* Dependem de WhatsApp para negociar servicos
* Nao conseguem divulgar facilmente seus servicos online

O **EasyBiz** resolve isso ao oferecer:

* Cadastro simples de negocio
* Exibicao publica do servico
* Busca inteligente por localizacao
* Chat interno para negociacao

---

## Quem usa o EasyBiz

**Dono do Negocio (Prestador)**
- Cria um perfil de servico
- Recebe novos pedidos
- Aceita ou recusa servicos
- Negocia via chat e finaliza o pedido

**Cliente**
- Busca servicos por categoria e localizacao
- Visualiza negocios disponiveis com avaliacoes
- Cria um pedido de servico (inicia uma "sala" de negociacao)
- Acompanha o status (Aberto, Aceito, Concluido, Cancelado)
- Avalia o servico apos conclusao

---

## Tecnologias

| Tecnologia | Versao | Uso |
|------------|--------|-----|
| Java | 17 (LTS) | Linguagem |
| Spring Boot | 3.4.2 | Framework |
| Spring Security | 3.4.2 | Autenticacao JWT |
| Spring Data JPA | 3.4.2 | ORM |
| Spring WebSocket | 3.4.2 | Chat em tempo real |
| Spring Mail | 3.4.2 | Envio de emails |
| Spring Actuator | 3.4.2 | Health check |
| Spring Validation | 3.4.2 | Validacao de dados |
| PostgreSQL | 15 | Banco de dados |
| Flyway | - | Migrations de banco |
| JJWT | 0.12.6 | Tokens JWT |
| Lombok | - | Reducao de boilerplate |
| SpringDoc OpenAPI | 2.7.0 | Swagger/Documentacao |
| H2 | - | Banco de testes |
| Docker | - | Containers |

---

## Seguranca & Autenticacao

O sistema opera com **Seguranca Stateless via JWT**:

- **JWT Service Centralizado**: Geracao e validacao de tokens HS256 com chaves de 256 bits
- **Stateless**: Nao ha sessao no servidor. Cada requisicao carrega sua credencial
- **Protecao Dupla**:
  - Camada HTTP: `JwtAuthenticationFilter` intercepta todas as chamadas REST
  - Camada WebSocket: `WebSocketJwtInterceptor` valida o token no handshake
- **Rate Limiting**: 10 req/min em endpoints sensiveis (login, cadastro)
- **Blindagem de Identidade**: IDs extraidos do Token JWT, nunca do payload
- **Excecoes Customizadas**: Respostas padronizadas com status HTTP corretos
- **Recuperacao de Senha**: Codigo de 6 digitos via email com expiracao de 15 min

---

## Funcionalidades V1

- Cadastro e login (JWT)
- Recuperacao de senha (email com codigo)
- Gestao de negocios
- Busca inteligente por geolocalizacao (Haversine, 30km)
- Criacao e gerenciamento de pedidos (workflow completo)
- Chat real-time seguro (WebSocket STOMP + historico REST)
- Avaliacoes com media automatica
- Gestao de fotos/logos via URL
- Tratamento global de erros padronizado
- Paginacao em listagens
- Rate limiting
- Database migrations (Flyway)
- Deploy via Docker + Railway

---

## Ciclo de Vida do Pedido

```
ABERTO --> ACEITO --> CONCLUIDO --> AVALIACAO
  |          |
  +--> RECUSADO (prestador)
  +--> CANCELADO (cliente)
```

**Regra de Ouro:** Nao e possivel concluir um pedido que nao foi aceito.

---

## Chat (Hibrido)

**REST (Historico)**
- `GET /pedidos/{id}/mensagens` - Carrega mensagens anteriores
- Apenas participantes do pedido podem visualizar

**WebSocket (Tempo Real)**
- Endpoint: `/ws-chat`
- Protocolo: STOMP sobre SockJS
- Topicos: `/topic/mensagens/{pedidoId}`
- Features: digitando, confirmacao de leitura, ultimo visto

---

## Estrutura do Projeto

```
br.com.easybiz
+-- config        # SecurityConfig, WebSocketConfig, OpenAPIConfig
+-- controller    # Endpoints REST (7 controllers)
+-- dto           # Records para transferencia de dados (19 DTOs)
+-- exception     # Excecoes customizadas + GlobalExceptionHandler
+-- model         # Entidades JPA (7 entidades)
+-- repository    # Interfaces Spring Data (7 repositories)
+-- security      # JwtService, Filters, Interceptors
+-- service       # Regras de negocio (8 services)
```

---

## Documentacao

| Arquivo | Descricao |
|---------|-----------|
| [API.md](docs/API.md) | Contrato completo da API |
| [INTEGRATION.md](docs/INTEGRATION.md) | Guia de integracao para frontend |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Arquitetura tecnica |
| [SECURITY.md](docs/SECURITY.md) | Seguranca e autenticacao |
| [WORKFLOW.md](docs/WORKFLOW.md) | Fluxos e estados |
| [CHANGELOG.md](docs/CHANGELOG.md) | Historico de mudancas |

**Swagger UI:** http://localhost:8080/swagger-ui/index.html

---

## Como Rodar

```bash
# 1. Subir PostgreSQL
docker-compose up -d

# 2. Configurar variaveis de ambiente (.env)
cp .env.example .env
# Editar .env com suas credenciais

# 3. Rodar o backend
./mvnw spring-boot:run

# 4. Acessar Swagger
# http://localhost:8080/swagger-ui/index.html
```

---

## Status do Projeto

**Backend V1 - Concluido**

Pronto para integracao com frontend (Kotlin Multiplatform / Compose).

---

*Projeto criado e mantido por Natanael Lopes*