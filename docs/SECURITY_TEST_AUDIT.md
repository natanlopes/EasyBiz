# EasyBiz — Auditoria de Segurança e Testes

Data: 2026-02-10
Escopo: revisão estática do backend + revisão da suíte de testes.

## Resumo executivo

Foram identificados **riscos críticos de autenticação/autorização** e **lacunas de testes** que podem causar:

- indisponibilidade de endpoints autenticados (erro 500);
- quebra de isolamento entre usuários (IDOR);
- envio/leitura de mensagens por usuários fora do pedido;
- falsa sensação de cobertura de testes (cenários importantes não protegidos).

## Achados críticos (prioridade P0/P1)

### 1) Inconsistência no principal do JWT (email vs id) — **P0**

- O token é emitido com `sub = email`.
- Vários controllers convertem `principal.getName()` para `Long`.
- Resultado: `NumberFormatException` em rotas protegidas que esperam id numérico.

**Impacto:** queda funcional de endpoints autenticados e falha de autorização baseada em usuário.

**Onde aparece:**
- `UsuarioController` (`/usuarios/me`, `/usuarios/me/foto`)
- `PedidoServicoController` (todos os PATCH/GET/POST com principal)
- `NegocioController` (`PATCH /negocios/{id}/logo`)
- `ChatController` (`digitando`, `confirmarLeitura`)
- `AvaliacaoController` (`POST /avaliacoes/pedido/{pedidoId}`)

### 2) Criação de negócio permite escolher `usuarioId` arbitrário — **P0**

- Endpoint de criação de negócio recebe `usuarioId` no body.
- Não usa o usuário autenticado para vincular o negócio.

**Impacto:** um usuário autenticado pode criar negócio em nome de outro usuário (IDOR / impersonação).

### 3) Endpoints de chat REST sem validação de pertencimento ao pedido — **P0**

- `GET /pedidos/{pedidoId}/mensagens` retorna histórico sem checar se chamador participa do pedido.
- `POST /pedidos/{pedidoId}/mensagens/lidas/{usuarioId}` permite marcar leitura para qualquer `usuarioId` via path param.

**Impacto:** vazamento de conversa e alteração indevida de estado de leitura.

### 4) `MensagemService.enviarMensagem` não valida participante do pedido — **P1**

- Serviço aceita usuário pelo email e salva mensagem no pedido sem confirmar se usuário é cliente ou prestador daquele pedido.

**Impacto:** usuário autenticado pode injetar mensagens em pedido de terceiros.

### 5) Tratamento global mapeia `RuntimeException` para 400 em todos os casos — **P1**

- Perde distinção entre `401`, `403`, `404`, `409`.
- Dificulta observabilidade e comportamento consistente do cliente.

## Lacunas importantes de segurança (P1/P2)

1. `LoginRequestDTO` sem validação (`@NotBlank`, `@Email`) — entrada malformada chega na camada de negócio.
2. `CriarPedidoServicoDTO` sem validações obrigatórias (`negocioId`, `descricao`).
3. `AvaliacaoDTO` sem faixa de nota (`@Min(1) @Max(5)`) e limite de comentário.
4. CORS com credenciais habilitadas e lista fixa de origens sem estratégia por ambiente.
5. `spring.jpa.hibernate.ddl-auto=update` em `application.properties` (risco operacional em produção).

## Estado atual de testes

### Cobertura existente

- Existe E2E com `MockMvc` cobrindo fluxo feliz principal (cadastro, login, negócio, pedido, avaliação).
- Existe teste mínimo de contexto (`contextLoads`).

### Problemas na cobertura

1. Não há testes unitários focados em autorização por recurso (owner checks por pedido/negócio).
2. Não há teste negativo para leitura de mensagens por não-participante.
3. Não há teste negativo para envio de mensagem por não-participante.
4. Não há teste negativo para criação de negócio com `usuarioId` de terceiro.
5. Há cenário importante desabilitado (`@Disabled`) para atualização de logo por não-dono.
6. Ausência de testes de validação de input (DTO constraints).
7. Ausência de testes de contrato para códigos HTTP de erro.

## Plano de correção recomendado

### Fase 1 (imediata)

1. **Padronizar identidade no JWT e no SecurityContext**
   - Opção A: manter `sub=email` e resolver id por email nos serviços.
   - Opção B: usar `sub=id` e manter email em claim extra.
   - Aplicar padrão único em REST e WebSocket.
2. **Remover `usuarioId` de criação de negócio**
   - Sempre usar usuário autenticado.
3. **Validar acesso aos pedidos no módulo de mensagens**
   - Em listar, enviar e marcar como lida.
4. **Corrigir endpoints para nunca receber `usuarioId` sensível via path/body**
   - Derivar sempre do token.

### Fase 2 (curto prazo)

1. Tipar exceções de domínio (`NotFound`, `Forbidden`, `Conflict`, `Validation`) e mapear status HTTP corretos.
2. Fortalecer validações de DTO com Bean Validation.
3. Separar perfil de produção para configurações seguras (`ddl-auto=validate`, logging reduzido).

### Fase 3 (testes)

1. Adicionar testes de autorização por recurso (pedido/mensagem/negócio).
2. Adicionar testes de validação de entrada.
3. Adicionar testes WebSocket para CONNECT/SUBSCRIBE com token inválido e usuário não participante.
4. Definir gate mínimo de cobertura para camadas de service/controller.

## Checklist objetivo para próximos commits

- [ ] Identidade JWT unificada (email/id) em todos os controllers/services.
- [ ] `POST /negocios` sem `usuarioId` no payload.
- [ ] Endpoints de mensagem com checagem de pertencimento.
- [ ] Remoção de `usuarioId` de endpoint de “marcar lidas”.
- [ ] DTOs com validação mínima e testes de erro 400.
- [ ] `GlobalExceptionHandler` com status semânticos.
- [ ] Reativar e ajustar testes negativos atualmente desabilitados.

## Como esta auditoria foi feita

- Leitura dos arquivos de segurança, controllers, services, DTOs e testes.
- Execução de tentativa de suíte (`./mvnw test -q`), bloqueada por falha de download do Maven Wrapper no ambiente.
