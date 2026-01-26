# EasyBiz 🚀

## 📌 Visão Geral

O **EasyBiz** é uma plataforma digital inspirada em modelos como iFood, porém voltada para **qualquer tipo de serviço**.

A proposta é simples: permitir que **qualquer pessoa**, mesmo sem conhecimento técnico, consiga **divulgar seus serviços**, ser encontrada por clientes da sua região e **negociar atendimentos de forma flexível**.

O foco inicial do projeto não é agenda rígida, mas sim **disponibilidade flexível e negociação direta**, refletindo a realidade de profissionais como:

* Pedreiros
* Mecânicos
* Barbeiros
* Personal trainers
* Mototáxis
* Prestadores de serviços em geral

---

## 🎯 Problema que o EasyBiz resolve

Hoje, muitos profissionais:

* Não sabem usar sistemas complexos de agenda
* Trabalham com horários variáveis
* Dependem de WhatsApp para negociar serviços
* Não conseguem divulgar facilmente seus serviços online

O **EasyBiz** resolve isso ao oferecer:

* Cadastro simples de negócio
* Exibição pública do serviço
* Horários flexíveis (informativos, não engessados)
* Chat interno para negociação

---

## 👥 Quem usa o EasyBiz
🔹 Dono do Negócio (Prestador)

- Cria um perfil de serviço e define disponibilidade.

- Recebe novos pedidos na aba de "Novos Contatos".

- Aceita ou Recusa serviços.

- Negocia via chat e finaliza o pedido.

## 🔹 Cliente
- Busca serviços por categoria.


- Visualiza negócios disponíveis.


- Cria um pedido de serviço (Inicia uma "sala" de negociação).


- Acompanha o status (Aberto, Aceito, Concluído).


## 🚀 Tecnologias
O projeto utiliza uma stack moderna e robusta para alta performance:

- Java 17+

- Spring Boot 3

- PostgreSQL (Docker)

- Spring Data JPA

- Lombok

- Spring Security (JWT Implementation)

- WebSocket (STOMP + SockJS)

- JJWT (Json Web Token 0.11.5)

- SpringDoc OpenAPI (Swagger)

##  🔐 Segurança & Autenticação (Implementado)
O sistema abandonou o modelo básico e agora opera com Segurança Stateless via JWT.

##  🛡️ Arquitetura de Segurança
- JWT Service Centralizado: Geração e validação de tokens assinados com algoritmo HS256 e chaves criptográficas de 256 bits.

- Stateless: Não há sessão no servidor. Cada requisição carrega sua credencial.

- Proteção Dupla:

- Camada HTTP: JwtAuthenticationFilter intercepta todas as chamadas REST.

- Camada WebSocket: WebSocketJwtInterceptor intercepta o handshake da conexão em tempo real, validando o token antes de permitir o acesso ao chat.

##  🚫 Blindagem de Identidade
O sistema ignora IDs enviados pelo Front-end para identificar o remetente. O ID do usuário é extraído diretamente do Token (Principal), impedindo que um usuário se passe por outro (Spoofing).

## 🧩 Conceitos principais do sistema
🏢 Negócio
- Representa o serviço cadastrado na plataforma (Ex: EasyBiz Barbearia). Um negócio pertence a um usuário.

📦 Pedido de Serviço (A "Sala de Negociação")
- É a entidade central que conecta Cliente e Prestador. Cada serviço novo gera um Pedido Único (ID), garantindo que negociações passadas não se misturem com as novas.

🔄 Ciclo de Vida do Pedido (Workflow)
- O pedido segue uma máquina de estados rigorosa para garantir a consistência do serviço:

### ABERTO: Cliente criou o pedido. Aguardando resposta.

### EM_NEGOCIACAO: (Opcional) Troca de mensagens antes do aceite.

### ACEITO: Prestador aceitou o serviço (via PATCH /aceitar).

### RECUSADO: Prestador não pode atender (via PATCH /recusar).

### CONCLUIDO: Serviço finalizado pelo prestador (via PATCH /concluir). Libera avaliação.

Regra de Ouro: Não é possível concluir um pedido que não foi aceito.

## 💬 Módulo de Chat (Híbrido)
O chat foi desenhado para ser resiliente e escalável, utilizando uma abordagem híbrida:

1️⃣ REST (Histórico)
- Endpoint: GET /pedidos/{id}/mensagens

- Função: Carrega todas as mensagens anteriores ao entrar na tela.

- Segurança: Garante que apenas os participantes do pedido (Cliente ou Dono do Negócio) visualizem o histórico.

## 2️⃣ WebSocket (Tempo Real)
- Endpoint: /ws-chat

- Protocolo: STOMP sobre SockJS.

- Tópicos: /topic/mensagens/{pedidoId}

- Segurança: O Interceptor valida o JWT no cabeçalho Authorization: Bearer ... durante a conexão. Se o token for inválido, o socket é desconectado imediatamente (Status 403).

## 🏗️ Estrutura do projeto (Backend)
O projeto segue uma arquitetura em camadas bem definidas:

```

br.com.easybiz
├── config        # SecurityConfig, WebSocketConfig, Interceptors
├── controller    # Endpoints REST (Auth, Chat, Pedido, Negocio)
├── dto           # Records para transferência de dados (Request/Response)
├── enums         # StatusPedido, TipoNegocio
├── model         # Entidades JPA (Usuario, Pedido, Mensagem)
├── repository    # Interfaces Spring Data
├── security      # JwtService, Filters
├── service       # Regras de negócio e Validações
└── EasybizApplication.java

```


## 📚 Documentação da API (Swagger)
A documentação viva está disponível e atualizada com as novas rotas de ciclo de vida.

📍 Acesso Local: http://localhost:8080/swagger-ui/index.html

## Principais recursos documentados:

- Auth: Login e geração de token.

- Pedidos: Criação e transição de status (Aceitar/Recusar).

- Chat: Histórico e envio de mensagens.

- Negócios: CRUD e busca.

## 🛠️ Status do projeto
- 🟢 Core Backend Finalizado

## Funcionalidades Entregues:

- ✅ Cadastro e Login (JWT)

- ✅ Gestão de Negócios

- ✅ Criação de Pedidos

- ✅ Chat Real-Time Seguro (WebSocket + JWT)

- ✅ Workflow de Status do Pedido

- ✅ Validação de segurança por Pedido

Próximas etapas (Foco no App Mobile):

📍 *Projeto criado e mantido por Natanael Lopes*
