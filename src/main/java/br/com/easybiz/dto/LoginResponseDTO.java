package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponseDTO", description = "Resposta de autenticação")
public class LoginResponseDTO {

    @Schema(description = "Token JWT para autenticação do user", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    // 1. Construtor Vazio (Obrigatório para o JSON/Jackson funcionar)
    public LoginResponseDTO() {
    }

    // 2. Construtor Cheio (Para você usar no Controller: new LoginResponseDTO(token))
    public LoginResponseDTO(String token) {
        this.token = token;
    }

    // 3. Getters e Setters (Obrigatórios para o Swagger ler o campo)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
