package br.com.easybiz.security;

import java.util.Collections;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.PedidoServicoRepository;
import br.com.easybiz.repository.UsuarioRepository;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final PedidoServicoRepository pedidoServicoRepository;
    private final UsuarioRepository usuarioRepository; // <--- NOVO: Precisamos disso para achar o ID pelo Email

    public WebSocketJwtInterceptor(
            JwtService jwtService,
            PedidoServicoRepository pedidoServicoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.jwtService = jwtService;
        this.pedidoServicoRepository = pedidoServicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // =================================================================
        // 🔍 FASE 1: CONEXÃO (CONNECT)
        // =================================================================
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            System.out.println("🔵 [WS] Nova tentativa de CONEXÃO recebida...");

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Valida o Token
                if (jwtService.tokenValido(token)) {

                    // CORREÇÃO 1: Extrai o EMAIL (String), não o ID
                    String emailUsuario = jwtService.extractUsername(token);
                    System.out.println("✅ [WS] Usuário (Email) extraído do Token: " + emailUsuario);

                    // Cria autenticação com o Email no Principal
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    emailUsuario,
                                    null,
                                    Collections.emptyList()
                            );
                    accessor.setUser(auth);
                } else {
                    System.out.println("❌ [WS] Token inválido ou expirado! Bloqueando.");
                    return null;
                }
            } else {
                System.out.println("❌ [WS] Header Authorization não encontrado ou sem Bearer.");
                return null;
            }
        }

        // =================================================================
        // 🔍 FASE 2: INSCRIÇÃO NA SALA (SUBSCRIBE)
        // =================================================================
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            System.out.println("🔵 [WS] Tentativa de SUBSCRIBE em: " + destination);

            if (destination != null && destination.startsWith("/topic/mensagens/")) {
                try {
                    // Pega o ID do pedido da URL
                    String[] parts = destination.split("/");
                    Long pedidoId = Long.valueOf(parts[3]);

                    // Verifica quem está tentando entrar
                    if (accessor.getUser() == null) {
                        throw new RuntimeException("Usuário não autenticado");
                    }

                    // CORREÇÃO 2: O Principal agora é o Email (String)
                    String emailUsuario = accessor.getUser().getName();

                    // Busca o ID real no banco para comparar permissão
                    Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                            .orElseThrow(() -> new RuntimeException("Usuário não encontrado no banco"));

                    Long usuarioId = usuario.getId();

                    System.out.println("🔎 [WS] Verificando permissão -> User ID: " + usuarioId + " no Pedido ID: " + pedidoId);

                    PedidoServico pedido = pedidoServicoRepository.findById(pedidoId)
                            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

                    Long idCliente = pedido.getCliente().getId();
                    Long idProfissional = pedido.getNegocio().getUsuario().getId();

                    boolean isCliente = idCliente.equals(usuarioId);
                    boolean isProfissional = idProfissional.equals(usuarioId);

                    if (!isCliente && !isProfissional) {
                        System.out.println("⛔ [WS] ACESSO NEGADO! O usuário " + emailUsuario + " não faz parte deste pedido.");
                        return null; // Bloqueia silenciosamente ou lança erro
                    }

                    System.out.println("✅ [WS] Acesso PERMITIDO para User " + emailUsuario);

                } catch (Exception e) {
                    System.out.println("❌ [WS] Erro na validação do SUBSCRIBE: " + e.getMessage());
                    return null; // Bloqueia a inscrição
                }
            }
        }

        return message;
    }
}