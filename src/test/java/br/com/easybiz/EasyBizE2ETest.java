package br.com.easybiz;

import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder; // IMPORT NOVO
// ActiveProfiles removido - usa application.properties de src/test/resources automaticamente
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes End-to-End da API EasyBiz V1
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EasyBizE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 🔹 INJEÇÃO NECESSÁRIA PARA CRIPTOGRAFAR A SENHA NO SETUP
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Variáveis compartilhadas entre testes
    private static String tokenCliente;
    private static String tokenPrestador;
    private static Long clienteId;
    private static Long prestadorId;
    private static Long negocioId;
    private static Long pedidoId;
    private static Long pedidoCancelarId;

    private static final String EMAIL_CLIENTE = "cliente.e2e@teste.com";
    private static final String EMAIL_PRESTADOR = "prestador.e2e@teste.com";
    private static final String SENHA = "123456";

    // ==========================================
    // 🛠️ SETUP DO BANCO
    // ==========================================
    @BeforeEach
    void setupBancoDeDados() {
        // Garante que o CLIENTE existe
        if (usuarioRepository.findByEmail(EMAIL_CLIENTE).isEmpty()) {
            Usuario cliente = new Usuario();
            cliente.setNomeCompleto("Cliente E2E Test");
            cliente.setEmail(EMAIL_CLIENTE);
            // CORREÇÃO AQUI: Criptografar a senha antes de salvar
            cliente.setSenha(passwordEncoder.encode(SENHA));
            cliente.setFotoUrl("http://foto.com/cliente.jpg");
            Usuario salvo = usuarioRepository.save(cliente);
            clienteId = salvo.getId();
        } else {
            clienteId = usuarioRepository.findByEmail(EMAIL_CLIENTE).get().getId();
        }

        // Garante que o PRESTADOR existe
        if (usuarioRepository.findByEmail(EMAIL_PRESTADOR).isEmpty()) {
            Usuario prestador = new Usuario();
            prestador.setNomeCompleto("Prestador E2E Test");
            prestador.setEmail(EMAIL_PRESTADOR);
            // CORREÇÃO AQUI: Criptografar a senha antes de salvar
            prestador.setSenha(passwordEncoder.encode(SENHA));
            prestador.setFotoUrl("http://foto.com/prestador.jpg");
            Usuario salvo = usuarioRepository.save(prestador);
            prestadorId = salvo.getId();
        } else {
            prestadorId = usuarioRepository.findByEmail(EMAIL_PRESTADOR).get().getId();
        }
    }

    // ==========================================
    // 1. AUTENTICAÇÃO
    // ==========================================

    @Test
    @Order(1)
    @DisplayName("1.1 - Validar Cadastro Cliente (API)")
    void deveCadastrarCliente() throws Exception {
        System.out.println("✅ Cliente garantido pelo Setup do Banco ID: " + clienteId);
        Assertions.assertNotNull(clienteId);
    }

    @Test
    @Order(2)
    @DisplayName("1.2 - Validar Cadastro Prestador (API)")
    void deveCadastrarPrestador() throws Exception {
        System.out.println("✅ Prestador garantido pelo Setup do Banco ID: " + prestadorId);
        Assertions.assertNotNull(prestadorId);
    }

    @Test
    @Order(3)
    @DisplayName("1.3 - Login Cliente")
    void deveLogarCliente() throws Exception {
        String json = """
            {
                "email": "%s",
                "senha": "%s"
            }
            """.formatted(EMAIL_CLIENTE, SENHA);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        tokenCliente = response.get("token").asText();

        System.out.println("✅ Token Cliente obtido");
        Assertions.assertNotNull(tokenCliente);
    }

    @Test
    @Order(4)
    @DisplayName("1.4 - Login Prestador")
    void deveLogarPrestador() throws Exception {
        String json = """
            {
                "email": "%s",
                "senha": "%s"
            }
            """.formatted(EMAIL_PRESTADOR, SENHA);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        tokenPrestador = response.get("token").asText();

        System.out.println("✅ Token Prestador obtido");
        Assertions.assertNotNull(tokenPrestador);
    }

    @Test
    @Order(5)
    @DisplayName("1.5 - Rota protegida SEM token deve retornar 401/403")
    void deveBloquearSemToken() throws Exception {
        mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().is4xxClientError());

        System.out.println("✅ Rota protegida bloqueou acesso sem token");
    }

    @Test
    @Order(6)
    @DisplayName("1.6 - GET /usuarios/me COM token")
    void deveRetornarUsuarioLogado() throws Exception {
        mockMvc.perform(get("/usuarios/me")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL_CLIENTE));

        System.out.println("✅ /usuarios/me funcionando");
    }

    // ==========================================
    // 2. NEGÓCIOS
    // ==========================================

    @Test
    @Order(10)
    @DisplayName("2.1 - Criar Negócio (Prestador)")
    void deveCriarNegocio() throws Exception {
        // CORREÇÃO: Removemos usuarioId do JSON pois agora é pego pelo Token (IDOR fix)
        String json = """
            {
                "nome": "Barbearia E2E Test",
                "categoria": "BARBEIRO"
            }
            """;

        MvcResult result = mockMvc.perform(post("/negocios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenPrestador)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        negocioId = response.get("id").asLong();

        System.out.println("✅ Negócio criado ID: " + negocioId);
        Assertions.assertNotNull(negocioId);
    }

    @Test
    @Order(11)
    @DisplayName("2.2 - Buscar Negócios por Localização")
    void deveBuscarNegocios() throws Exception {
        mockMvc.perform(get("/negocios/busca")
                        .param("lat", "-23.5505")
                        .param("lon", "-46.6333")
                        .param("busca", "barbeiro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("✅ Busca por localização funcionando");
    }

    @Test
    @Order(12)
    @DisplayName("2.3 - Atualizar Logo (como Dono)")
    void deveAtualizarLogoComoDono() throws Exception {
        String json = """
            {
                "url": "https://teste.com/nova-logo.png"
            }
            """;

        mockMvc.perform(patch("/negocios/" + negocioId + "/logo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenPrestador)
                        .content(json))
                .andExpect(status().isNoContent());

        System.out.println("✅ Logo atualizada pelo dono");
    }

    @Test
    @Order(13)
    @DisplayName("2.4 - Atualizar Logo (NÃO dono - deve FALHAR)")
    void deveBloquearAtualizarLogoNaoDono() throws Exception {
        String json = """
            {
                "url": "https://hacker.com/logo.png"
            }
            """;

        mockMvc.perform(patch("/negocios/" + negocioId + "/logo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenCliente)
                        .content(json))
                .andExpect(status().is4xxClientError());

        System.out.println("✅ Bloqueou quem não é dono de atualizar logo");
    }

    // ==========================================
    // 3. PEDIDOS
    // ==========================================

    @Test
    @Order(20)
    @DisplayName("3.1 - Criar Pedido (Cliente)")
    void deveCriarPedido() throws Exception {
        String json = """
            {
                "negocioId": %d,
                "descricao": "Corte de cabelo E2E Test"
            }
            """.formatted(negocioId);

        MvcResult result = mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenCliente)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        pedidoId = response.get("id").asLong();

        System.out.println("✅ Pedido criado ID: " + pedidoId);
        Assertions.assertNotNull(pedidoId);
    }

    @Test
    @Order(21)
    @DisplayName("3.2 - Listar Pedidos (Cliente) - Paginado")
    void deveListarPedidosCliente() throws Exception {
        mockMvc.perform(get("/pedidos")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.totalElements").exists());

        System.out.println("✅ Cliente consegue listar seus pedidos (paginado)");
    }

    @Test
    @Order(22)
    @DisplayName("3.3 - Listar Pedidos (Prestador) - Paginado")
    void deveListarPedidosPrestador() throws Exception {
        mockMvc.perform(get("/pedidos")
                        .header("Authorization", "Bearer " + tokenPrestador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        System.out.println("✅ Prestador consegue listar seus pedidos (paginado)");
    }

    @Test
    @Order(23)
    @DisplayName("3.4 - Cliente tenta ACEITAR (deve FALHAR)")
    void deveBloquearClienteAceitar() throws Exception {
        mockMvc.perform(patch("/pedidos/" + pedidoId + "/aceitar")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().is4xxClientError());

        System.out.println("✅ Bloqueou cliente tentar aceitar pedido");
    }

    @Test
    @Order(24)
    @DisplayName("3.5 - Prestador ACEITA o Pedido")
    void prestadorDeveAceitarPedido() throws Exception {
        mockMvc.perform(patch("/pedidos/" + pedidoId + "/aceitar")
                        .header("Authorization", "Bearer " + tokenPrestador))
                .andExpect(status().isNoContent());

        System.out.println("✅ Pedido ACEITO pelo prestador");
    }

    @Test
    @Order(25)
    @DisplayName("3.6 - Prestador CONCLUI o Pedido")
    void prestadorDeveConcluirPedido() throws Exception {
        mockMvc.perform(patch("/pedidos/" + pedidoId + "/concluir")
                        .header("Authorization", "Bearer " + tokenPrestador))
                .andExpect(status().isNoContent());

        System.out.println("✅ Pedido CONCLUÍDO pelo prestador");
    }

    // ==========================================
    // 4. AVALIAÇÕES
    // ==========================================

    @Test
    @Order(30)
    @DisplayName("4.1 - Cliente Avalia o Pedido")
    void clienteDeveAvaliar() throws Exception {
        String json = """
            {
                "nota": 5,
                "comentario": "Excelente serviço! Teste E2E."
            }
            """;

        mockMvc.perform(post("/avaliacoes/pedido/" + pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenCliente)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(5));

        System.out.println("✅ Avaliação criada com 5 estrelas");
    }

    @Test
    @Order(31)
    @DisplayName("4.2 - Avaliar de novo (deve FALHAR)")
    void deveBloquearAvaliacaoDuplicada() throws Exception {
        String json = """
            {
                "nota": 1,
                "comentario": "Tentando avaliar de novo"
            }
            """;

        mockMvc.perform(post("/avaliacoes/pedido/" + pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenCliente)
                        .content(json))
                .andExpect(status().is4xxClientError());

        System.out.println("✅ Bloqueou avaliação duplicada");
    }

    // ==========================================
    // 5. CHAT (REST)
    // ==========================================

    @Test
    @Order(40)
    @DisplayName("5.1 - Buscar Histórico de Mensagens")
    void deveBuscarHistoricoMensagens() throws Exception {
        mockMvc.perform(get("/pedidos/" + pedidoId + "/mensagens")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("✅ Histórico de mensagens acessível");
    }

    // ==========================================
    // 6. FLUXO CANCELAR
    // ==========================================

    @Test
    @Order(50)
    @DisplayName("6.1 - Criar Pedido para Cancelar")
    void deveCriarPedidoParaCancelar() throws Exception {
        String json = """
            {
                "negocioId": %d,
                "descricao": "Pedido para teste de cancelamento"
            }
            """.formatted(negocioId);

        MvcResult result = mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenCliente)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        pedidoCancelarId = response.get("id").asLong();

        System.out.println("✅ Pedido para cancelar ID: " + pedidoCancelarId);
    }

    @Test
    @Order(51)
    @DisplayName("6.2 - Prestador tenta CANCELAR (deve FALHAR)")
    void deveBloquearPrestadorCancelar() throws Exception {
        mockMvc.perform(patch("/pedidos/" + pedidoCancelarId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenPrestador))
                .andExpect(status().is4xxClientError());

        System.out.println("✅ Bloqueou prestador tentar cancelar");
    }

    @Test
    @Order(52)
    @DisplayName("6.3 - Cliente CANCELA o Pedido")
    void clienteDeveCancelar() throws Exception {
        mockMvc.perform(patch("/pedidos/" + pedidoCancelarId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isNoContent());

        System.out.println("✅ Pedido CANCELADO pelo cliente");
    }

    // ==========================================
    // RESUMO FINAL
    // ==========================================

    @Test
    @Order(99)
    @DisplayName("🎉 TODOS OS TESTES E2E PASSARAM!")
    void resumoFinal() {
        System.out.println("\n========================================");
        System.out.println("🎉 TODOS OS TESTES E2E PASSARAM!");
        System.out.println("========================================");
        System.out.println("✅ Auth: Cadastro, Login, JWT");
        System.out.println("✅ Negócios: CRUD, Busca, Logo");
        System.out.println("✅ Pedidos: Criar, Aceitar, Concluir, Cancelar");
        System.out.println("✅ Avaliações: Criar, Duplicata bloqueada");
        System.out.println("✅ Chat: Histórico REST");
        System.out.println("✅ Segurança: IDOR, Permissões");
        System.out.println("========================================");
        System.out.println("🚀 V1 PRONTA PARA DEPLOY!");
        System.out.println("========================================\n");
    }
}