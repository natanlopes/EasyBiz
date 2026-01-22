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

### 🔹 Dono do Negócio

* Cria um perfil de serviço
* Define horários ou disponibilidade
* Recebe pedidos de serviço
* Negocia diretamente com o cliente via chat

## 🚀 Tecnologias
- **Java 17+**
- **Spring Boot 3**
- **PostgreSQL** (Docker)
- **Spring Data JPA**
- **Lombok**
- **Spring Security** (Básico configurado)

### 🔹 Cliente

* Busca serviços por categoria
* Visualiza negócios disponíveis
* Consulta horários aproximados
* Envia pedido de serviço
* Negocia valores, datas e detalhes no chat

---

## 🧩 Conceitos principais do sistema

### 🏢 Negócio

Representa o serviço cadastrado na plataforma.

Exemplos:

* EasyBiz Barbearia
* Mecânico João
* Pedreiro Carlos

Um negócio pertence a **um usuário**.

---

### ⚙️ Configuração do Negócio (NegocioConfig)

Define informações **básicas de funcionamento**, como:

* Horário de abertura e fechamento (opcional)
* Dias da semana que costuma atender
* Se aceita agendamentos

⚠️ Importante: esses horários **não são obrigatórios nem rígidos**. Eles servem apenas como **referência para o cliente**.

---

### 📦 Pedido de Serviço

É o primeiro contato entre cliente e negócio.

Contém:

* Cliente que solicitou
* Negócio escolhido
* Descrição do serviço
* Data desejada (opcional)
* Status do pedido (ABERTO, EM_NEGOCIACAO, FECHADO, CANCELADO)

O pedido funciona como um **"inbox"**, onde a conversa começa.

---

### 💬 Mensagens (Chat)

Após criar um pedido, cliente e dono do negócio conversam através de mensagens.

Esse chat serve para:

* Negociar valores
* Ajustar datas
* Esclarecer dúvidas
* Confirmar ou cancelar serviços

---

## 🔄 Fluxo principal do sistema

1. Usuário se cadastra
2. Usuário cria um negócio
3. Negócio define (opcionalmente) horários e dias
4. Cliente busca serviços por categoria
5. Cliente escolhe um negócio
6. Cliente cria um pedido de serviço
7. Cliente e dono negociam via chat
8. Serviço é fechado ou cancelado

---

## 🏗️ Estrutura do projeto (Backend)

O projeto segue uma arquitetura em camadas:

```
br.com.easybiz
├── config        # Segurança, Swagger, configurações gerais
├── controller    # Controllers REST (API)
├── dto           # DTOs de entrada e saída
├── model         # Entidades JPA
├── repository    # Repositórios (JPA)
├── service       # Regras de negócio
└── EasybizApplication.java
```

---

## 🔐 Segurança

Atualmente o projeto está em **modo de desenvolvimento**, com:

* CSRF desabilitado
* Rotas abertas para cadastro e testes

⚠️ A segurança será evoluída futuramente com:

* Autenticação JWT
* Autorização por perfil
* Proteção de rotas

---

## 📚 Documentação da API

O projeto utilizará **Swagger / OpenAPI** para documentação viva da API.

Isso permitirá:

* Visualizar todas as rotas
* Testar endpoints
* Facilitar integração com frontend

📌 *Swagger será configurado na próxima etapa.*
http://localhost:8080/swagger-ui/index.html

## Usamos anotações do springdoc-openapi:

@Tag

@Operation

@ApiResponses

@Scheme


## 🛠️ Status do projeto

🟡 **Em desenvolvimento ativo**

Funcionalidades já implementadas:

* Cadastro de usuários
* Cadastro de negócios
* Configuração básica de negócio
* Criação de pedido de serviço

Próximas etapas:

* Módulo de mensagens (chat)
* Swagger
* Melhorias de segurança
* Filtros por região
* Destaque de negócios

---

## 🤝 Contribuição

Este projeto está em fase inicial.

Sugestões, melhorias e feedback são bem-vindos.

---

## 📌 Visão futura

O EasyBiz pretende se tornar um **marketplace de serviços flexível**, simples e acessível, conectando pessoas a profissionais de forma rápida e humana.

---

📍 *Projeto criado e mantido por Natanael Lopes*
