package br.com.easybiz.controller;

import br.com.easybiz.dto.CriarNegocioDTO;
import br.com.easybiz.model.Negocio;
import br.com.easybiz.service.NegocioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/negocios")
@RequiredArgsConstructor
public class NegocioController {

    private final NegocioService negocioService;
    public NegocioController(NegocioService negocioService) {
        this.negocioService = negocioService;
    }

    @PostMapping
    public ResponseEntity<Negocio> criar(@RequestBody @Valid CriarNegocioDTO dto) {
        Negocio negocio = negocioService.criarNegocio(
                dto.usuarioId(),
                dto.nome(),
                dto.tipo()
        );
        return ResponseEntity.ok(negocio);
    }
}

