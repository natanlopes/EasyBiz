package br.com.easybiz.controller;

import br.com.easybiz.dto.CriarUsuarioDTO;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")

public class UsuarioController {

    private final UsuarioService service;


    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody @Valid CriarUsuarioDTO dto) {
        Usuario novoUsuario = service.criarUsuario(dto);
        return ResponseEntity.ok(novoUsuario);
    }
}