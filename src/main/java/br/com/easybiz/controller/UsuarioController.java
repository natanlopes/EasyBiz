package br.com.easybiz.controller;

import br.com.easybiz.dto.CriarUsuarioDTO;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Tag(name = "1. Usuários", description = "Gestão dos donos de negócios (Prestadores)")
@RestController
@RequestMapping("/usuarios")

public class UsuarioController {

    private final UsuarioService service;


    public UsuarioController(UsuarioService service) {
        this.service = service;
    }
    @Operation(summary = "Cadastrar Usuário", description = "Cria uma conta de prestador de serviço.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos (email duplicado ou campos em branco)")
    })
    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody @Valid CriarUsuarioDTO dto) {
        Usuario novoUsuario = service.criarUsuario(dto);
        return ResponseEntity.ok(novoUsuario);
    }
}