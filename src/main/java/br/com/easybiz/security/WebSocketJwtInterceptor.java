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
import br.com.easybiz.repository.PedidoServicoRepository;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final PedidoServicoRepository pedidoServicoRepository;

    public WebSocketJwtInterceptor(JwtService jwtService, PedidoServicoRepository pedidoServicoRepository) {
        this.jwtService = jwtService;
        this.pedidoServicoRepository = pedidoServicoRepository;
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

                // LOG DE DEBUG
                boolean isTokenValido = jwtService.tokenValido(token);
                System.out.println("❓ [WS] Token é válido? " + isTokenValido);

                if (isTokenValido) {
                    Long usuarioId = jwtService.extractUserId(token);
                    System.out.println("✅ [WS] Usuário ID extraído do Token: " + usuarioId);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    usuarioId.toString(),
                                    null,
                                    Collections.emptyList()
                            );
                    accessor.setUser(auth);
                } else {
                    System.out.println("❌ [WS] Token inválido ou expirado! Bloqueando.");
                    return null; // Retorna null para cancelar a conexão
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
                        System.out.println("❌ [WS] Erro: Usuário sem sessão (Auth falhou antes).");
                        throw new RuntimeException("Usuário não autenticado");
                    }

                    Long usuarioId = Long.valueOf(accessor.getUser().getName());
                    System.out.println("🔎 [WS] Verificando permissão -> User ID: " + usuarioId + " no Pedido ID: " + pedidoId);

                    PedidoServico pedido = pedidoServicoRepository.findById(pedidoId)
                            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

                    // LOGS DOS ENVOLVIDOS
                    Long idCliente = pedido.getCliente().getId();
                    Long idProfissional = pedido.getNegocio().getUsuario().getId();
                    System.out.println("ℹ️ [WS] Dados do Pedido -> ClienteID: " + idCliente + " | ProfissionalID: " + idProfissional);

                    boolean isCliente = idCliente.equals(usuarioId);
                    boolean isProfissional = idProfissional.equals(usuarioId);

                    if (!isCliente && !isProfissional) {
                        System.out.println("⛔ [WS] ACESSO NEGADO! O usuário " + usuarioId + " não faz parte deste pedido.");
                        throw new RuntimeException("Acesso negado ao chat");
                    }

                    System.out.println("✅ [WS] Acesso PERMITIDO para User " + usuarioId);

                } catch (Exception e) {
                    System.out.println("❌ [WS] Erro na validação do SUBSCRIBE: " + e.getMessage());
                    return null; // Bloqueia a inscrição
                }
            }
        }

        return message;
    }
}