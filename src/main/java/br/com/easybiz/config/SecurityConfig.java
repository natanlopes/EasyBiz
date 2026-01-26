package br.com.easybiz.config;

import br.com.easybiz.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                // 🟢 2. LIBERA A PORTA DE ENTRADA (LOGIN)
                .requestMatchers("/auth/**").permitAll() 
                
                // Swagger e Docs
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Cadastro de usuários
                .requestMatchers(HttpMethod.POST, "/usuarios/**").permitAll()
                
                // 🔴 CORREÇÃO AQUI: Agora liberamos o endereço certo!
                .requestMatchers("/ws-chat/**").permitAll()
                
                // Erros do Spring
                .requestMatchers("/error").permitAll()

                // Rotas temporárias
                .requestMatchers("/negocios/**", "/clientes/**", "/pedidos/**").permitAll()

                // 🔒 O resto exige estar logado
                .anyRequest().authenticated()
            )
            // 🟢 3. ATIVA O FILTRO QUE LÊ O TOKEN
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}