# 📋 Changelog

Todas as mudanças notáveis do projeto EasyBiz serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [1.0.1] - 2025-02-05

### 🔒 Segurança
- **JwtService**: Removido secret hardcoded, agora usa variável de ambiente `${JWT_SECRET}`
- **SecurityConfig**: Removido `.permitAll()` temporário das rotas `/negocios/**` e `/pedidos/**`

### ✨ Adicionado
- **AvaliacaoResponseDTO**: Novo DTO para retorno seguro de avaliações (sem expor dados sensíveis)
- **StatusPedido.CANCELADO**: Novo status no enum para pedidos cancelados
- **PedidoServicoService.cancelar()**: Método para cliente cancelar pedidos

### 📝 Documentação
- **AvaliacaoResponseDTO**: Documentação completa com `@Schema` (Swagger/OpenAPI)
- **AvaliacaoController**: Anotações `@Operation`, `@ApiResponse`, `@Parameter` completas
- **AvaliacaoService**: JavaDoc detalhado em todos os métodos
- **SECURITY.md**: Atualizado com BCrypt, variáveis de ambiente e checklist
- **ARCHITECTURE.md**: Adicionada seção de arquitetura de segurança

### 🔄 Alterado
- **AvaliacaoService.avaliarPedido()**: Agora retorna `AvaliacaoResponseDTO` em vez de `Avaliacao`
- **AvaliacaoController.avaliar()**: Atualizado para usar o novo DTO de resposta
- **.env.example**: Expandido com todas as variáveis necessárias

### 🗂️ Arquivos Modificados

```
src/main/java/br/com/easybiz/
├── dto/
│   └── AvaliacaoResponseDTO.java      ← NOVO
├── service/
│   └── AvaliacaoService.java          ← MODIFICADO (retorna DTO)
├── controller/
│   └── AvaliacaoController.java       ← MODIFICADO (documentação Swagger)
├── security/
│   └── JwtService.java                ← MODIFICADO (usa env var)
├── model/
│   └── StatusPedido.java              ← MODIFICADO (+ CANCELADO)
docs/
├── SECURITY.md                        ← MODIFICADO
├── ARCHITECTURE.md                    ← MODIFICADO
└── CHANGELOG.md                       ← NOVO
.env.example                           ← MODIFICADO
```

---

## [1.0.0] - 2025-01-30

### ✨ Release Inicial - MVP V1

#### Funcionalidades Core
- **Autenticação**: JWT com Spring Security
- **Usuários**: Cadastro, login, perfil
- **Negócios**: CRUD completo, busca por geolocalização (Haversine)
- **Pedidos**: Workflow completo (ABERTO → ACEITO → CONCLUIDO)
- **Chat**: WebSocket STOMP em tempo real + histórico REST
- **Avaliações**: Sistema de notas com atualização de média

#### Documentação
- API.md: Contrato completo da API
- ARCHITECTURE.md: Visão técnica do sistema
- SECURITY.md: Documentação de segurança
- WORKFLOW.md: Fluxos e estados

#### Infraestrutura
- Docker Compose para PostgreSQL
- Swagger UI para documentação interativa
- Global Exception Handler

---

## Convenção de Commits

Este projeto segue a convenção [Conventional Commits](https://www.conventionalcommits.org/):

| Tipo | Descrição |
|------|-----------|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `docs` | Apenas documentação |
| `style` | Formatação (não afeta código) |
| `refactor` | Refatoração sem mudar comportamento |
| `perf` | Melhoria de performance |
| `test` | Adição/correção de testes |
| `chore` | Tarefas de manutenção |
| `security` | Correções de segurança |

### Exemplos de Commits

```bash
# Nova funcionalidade
git commit -m "feat(avaliacao): adiciona AvaliacaoResponseDTO para retorno seguro"

# Correção de segurança
git commit -m "security(jwt): remove secret hardcoded, usa variável de ambiente"

# Documentação
git commit -m "docs(swagger): adiciona @Schema em AvaliacaoResponseDTO"

# Múltiplas mudanças relacionadas
git commit -m "refactor(avaliacao): sanitiza response e documenta API

- Cria AvaliacaoResponseDTO com @Schema completo
- Atualiza AvaliacaoService para retornar DTO
- Adiciona JavaDoc em todos os métodos
- Documenta AvaliacaoController com @Operation e @ApiResponse

BREAKING CHANGE: endpoint /avaliacoes/pedido/{id} agora retorna DTO simplificado"
```

---

## Versionamento

- **MAJOR** (1.x.x): Mudanças incompatíveis na API
- **MINOR** (x.1.x): Novas funcionalidades retrocompatíveis
- **PATCH** (x.x.1): Correções de bugs retrocompatíveis
