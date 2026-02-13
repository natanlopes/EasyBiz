# 🏛️ EasyBiz Architecture

## 1. Visão Geral
O EasyBiz utiliza uma **Arquitetura em Camadas (Layered Architecture)** baseada no ecossistema Spring Boot 3. O sistema foi projetado para ser **Stateless** (sem sessão no servidor) e altamente escalável, focado em alta concorrência para o Chat em Tempo Real.

## 2. Stack Tecnológica
- **Linguagem:** Java 17 (LTS)
- **Framework:** Spring Boot 3.4+
- **Database:** PostgreSQL (via Docker)
- **ORM:** Spring Data JPA + Hibernate
- **Real-Time:** WebSocket (STOMP sobre SockJS)
- **Security:** Spring Security + JWT (JJWT 0.11.5) + BCrypt

## 3. Padrão de Chat Híbrido (Hybrid Chat Pattern)
Para garantir performance e persistência, adotamos uma abordagem híbrida:

| Camada | Tecnologia | Responsabilidade |
| :--- | :--- | :--- |
| **Histórico** | REST API (HTTP) | Carregar mensagens antigas ao abrir a tela. Garante que nada seja perdido. |
| **Ao Vivo** | WebSocket (STOMP) | Entrega instantânea de novas mensagens sem *polling*. |

**Decisão de Design:**
Não usamos o WebSocket para buscar histórico para evitar sobrecarga no broker de mensagens. O banco de dados relacional (Postgres) é mais eficiente para consultas paginadas de histórico via HTTP.

## 4. Modelagem de Dados (ER Simplificado)
- **Usuario:** Entidade base (Cliente ou Prestador).
- **Negocio:** Pertence a um Usuario. Define a "vitrine" do serviço.
- **PedidoServico:** A "Sala de Reunião". Liga um Cliente a um Negocio.
- **Mensagem:** Pertence a um PedidoServico.
- **Avaliacao:** Gerada apenas após o ciclo de vida do Pedido ser concluído.

## 5. Escalabilidade
O sistema está preparado para rodar em containers (Docker).
Como a autenticação é via JWT (Stateless), é possível subir múltiplas instâncias da API atrás de um Load Balancer sem quebrar a sessão do usuário.

## 6. Busca Inteligente
A busca utiliza cálculo de distância (Haversine) diretamente no banco,
reduzindo carga no backend e garantindo performance.

A nota média do negócio é recalculada a cada nova avaliação e persistida na entidade Negocio para otimizar buscas e rankings.

---

## 7. 🔐 Arquitetura de Segurança

### Camadas de Proteção

```
┌─────────────────────────────────────────────────────────────┐
│                      CLIENTE (App/Web)                       │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY / NGINX                       │
│                    (HTTPS, Rate Limiting)                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
          ┌────────────────┴────────────────┐
          │                                 │
          ▼                                 ▼
┌─────────────────────┐         ┌─────────────────────┐
│   REST Endpoints    │         │  WebSocket Server   │
│ JwtAuthFilter       │         │ JwtChannelIntercept │
└─────────┬───────────┘         └─────────┬───────────┘
          │                               │
          └───────────────┬───────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   SPRING SECURITY CONTEXT                   │
│                   (Principal = email do usuário)            │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                           │
│              (Validação de Ownership/IDOR)                   │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                     REPOSITORY LAYER                         │
│                      (JPA/Hibernate)                         │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      PostgreSQL                              │
│               (Senhas em BCrypt Hash)                        │
└─────────────────────────────────────────────────────────────┘
```

### Princípios de Segurança

| Princípio | Implementação |
|-----------|--------------|
| **Defense in Depth** | Múltiplas camadas de validação |
| **Least Privilege** | Acesso apenas ao necessário |
| **Secure by Default** | Endpoints bloqueados por padrão |
| **No Secrets in Code** | Variáveis de ambiente |

### Configurações Sensíveis

```properties
# application.properties - Usa variáveis de ambiente
spring.datasource.password=${DB_PASSWORD}
api.security.token.secret=${JWT_SECRET}
```

> 📖 Detalhes completos em [SECURITY.md](./SECURITY.md)

---

## 8. Global Exception Handling

Foi adicionada uma camada de `@RestControllerAdvice` para interceptar exceções e garantir que o backend nunca retorne stack traces para o cliente.

Benefícios:
- Melhor UX no App
- Backend previsível
- Mensagens amigáveis para o Flutter
- Sem vazamento de informações técnicas

---

## 9. Estrutura de Pastas

```
src/main/java/br/com/easybiz/
├── config/           # Configurações (Security, WebSocket, OpenAPI)
├── controller/       # REST Controllers
├── dto/              # Data Transfer Objects
├── exception/        # Tratamento global de erros
├── model/            # Entidades JPA
├── repository/       # Spring Data JPA Repositories
├── security/         # JWT Filter, Service, Interceptors
└── service/          # Regras de negócio

docs/
├── API.md            # Contrato da API
├── ARCHITECTURE.md   # Este arquivo
├── SECURITY.md       # Documentação de segurança
└── WORKFLOW.md       # Fluxos e estados
```
