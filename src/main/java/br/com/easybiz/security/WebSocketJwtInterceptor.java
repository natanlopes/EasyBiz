package br.com.easybiz.security;

import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.repository.PedidoServicoRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final PedidoServicoRepository pedidoServicoRepository;

    public WebSocketJwtInterceptor(
            JwtService jwtService,
            PedidoServicoRepository pedidoServicoRepository
    ) {
        this.jwtService = jwtService;
        this.pedidoServicoRepository = pedidoServicoRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // 🔐 CONNECT → autenticação
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                if (jwtService.tokenValido(token)) {

                    Long usuarioId = jwtService.extractUserId(token);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    usuarioId.toString(), // vira Principal.getName()
                                    null,
                                    Collections.emptyList()
                            );

                    accessor.setUser(auth);
                }
            }
        }

        // 🔒 SUBSCRIBE → autorização da sala
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();
            if (destination == null) return message;

            if (destination.startsWith("/topic/mensagens/")) {

                Long pedidoId = Long.valueOf(destination.split("/")[3]);

                if (accessor.getUser() == null) {
                    throw new RuntimeException("Usuário não autenticado");
                }

                Long usuarioId =
                        Long.valueOf(accessor.getUser().getName());

                PedidoServico pedido = pedidoServicoRepository.findById(pedidoId)
                        .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

                boolean isCliente =
                        pedido.getCliente().getId().equals(usuarioId);

                boolean isProfissional =
                        pedido.getNegocio()
                              .getUsuario()
                              .getId()
                              .equals(usuarioId);

                if (!isCliente && !isProfissional) {
                    throw new RuntimeException("Acesso negado ao chat");
                }
            }
        }

        return message;
    }
}
