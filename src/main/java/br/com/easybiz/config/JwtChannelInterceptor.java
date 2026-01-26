package br.com.easybiz.config; // Ajuste o pacote se necessário

import br.com.easybiz.security.JwtService;
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
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public JwtChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Só valida se for conexão inicial (CONNECT)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // 1. Usa o método correto que existe no seu JwtService
                if (jwtService.tokenValido(token)) {
                    
                    // 2. Extrai apenas o ID (Long)
                    Long usuarioId = jwtService.extractUserId(token);

                    // 3. Cria a autenticação usando o ID como Principal
                    // Passamos lista vazia de authorities pois não estamos carregando roles do banco aqui
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    usuarioId, 
                                    null, 
                                    Collections.emptyList()
                            );

                    accessor.setUser(authentication);
                }
            }
        }

        return message;
    }
}