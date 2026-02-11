package br.com.easybiz.security;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import br.com.easybiz.security.JwtService; // ✅ Ajustado para o serviço correto

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketJwtInterceptor.class);

    private final JwtService jwtService; // ✅ Injeção do JwtService

    public WebSocketJwtInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.debug("🔵 [WS] Nova tentativa de CONEXÃO recebida...");

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // 1. Valida se o token é autêntico (Assinatura + Expiração)
                if (jwtService.tokenValido(token)) {

                    // 2. Extrai o email para criar a identidade
                    String email = jwtService.extractUsername(token);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

                    accessor.setUser(authentication);
                    log.info("✅ [WS] Conexão autenticada para: {}", email);

                } else {
                    log.warn("❌ [WS] Token inválido ou expirado! Conexão recusada.");
                    return null; // ⛔ ISSO É IMPORTANTE: Bloqueia a conexão!
                }
            } else {
                log.warn("⚠️ [WS] Cabeçalho Authorization ausente. Conexão recusada.");
                return null; // ⛔ Bloqueia conexão sem token
            }
        }
        return message;
    }
}