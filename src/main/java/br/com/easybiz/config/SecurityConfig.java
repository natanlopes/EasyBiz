package br.com.easybiz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import br.com.easybiz.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:8080",
            "http://10.0.2.2:8080",
            "https://easybiz-staging.up.railway.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // 🟢 2. LIBERA A PORTA DE ENTRADA (LOGIN)
                .requestMatchers("/auth/**").permitAll()

                // Swagger e Docs
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // 🟢 LIBERA O ACTUATOR (Essencial para o Railway não matar o app)
                .requestMatchers("/actuator/**").permitAll()  // Permite acesso ao Actuator para monitoramento

                // Cadastro de usuários
                .requestMatchers(HttpMethod.POST, "/usuarios/**").permitAll()

                // WebSocket
                .requestMatchers("/ws-chat/**").permitAll()

                // Erros do Spring
                .requestMatchers("/error").permitAll()

                // Rotas temporárias e públicas
                .requestMatchers(HttpMethod.GET, "/negocios/**").permitAll() 
                
                // 🔒 Rotas protegidas
                .requestMatchers("/negocios/**").authenticated() 
                .requestMatchers("/pedidos/**").authenticated() 

                // 🔒 O resto exige estar logado
                .anyRequest().authenticated()
            )
            // 🟢 3. ATIVA O FILTRO QUE LÊ O TOKEN
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}