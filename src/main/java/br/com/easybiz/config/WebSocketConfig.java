package br.com.easybiz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import br.com.easybiz.security.WebSocketJwtInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketJwtInterceptor interceptor;

    // Injetamos APENAS o interceptador correto (o novo)
    public WebSocketConfig(WebSocketJwtInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Registra o interceptador de segurança
        registration.interceptors(interceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                // 🔒 SEGURANÇA (Item 4.3): Restringe origens permitidas
                .setAllowedOriginPatterns(
                        "http://localhost:3000",       // Frontend React/Next local
                        "http://localhost:4200",       // Frontend Angular local (se usar)
                        "http://localhost:8080",       // Swagger UI / Backend local
                        "https://*.up.railway.app",    // Produção (Railway)
                        "https://easybiz-frontend.vercel.app" // Exemplo de Front em produção (ajuste se tiver)
                )
                .withSockJS();
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
}