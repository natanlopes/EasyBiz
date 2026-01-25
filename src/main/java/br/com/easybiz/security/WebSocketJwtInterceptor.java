package br.com.easybiz.security;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketJwtInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    // 1. Valida e extrai ID direto do token (Zero DB Hits)
                    Long usuarioId = jwtService.extractUserId(token);
                    
                    // 2. Coloca na sessão
                    accessor.getSessionAttributes().put("usuarioId", usuarioId);
                    
                    System.out.println("✅ Autenticado via JWT (ID: " + usuarioId + ")");
                } catch (Exception e) {
                    System.out.println("❌ Token inválido no WebSocket");
                    return null; // Nega conexão
                }
            } else {
                return null; // Nega sem token
            }
        }
        return message;
    }
}