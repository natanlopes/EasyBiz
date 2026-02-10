package br.com.easybiz.controller;

import java.security.Principal;
import java.util.List;

import br.com.easybiz.model.Usuario; // Importante
import br.com.easybiz.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.easybiz.dto.CriarPedidoServicoDTO;
import br.com.easybiz.dto.PedidoServicoResponseDTO;
import br.com.easybiz.service.PedidoServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pedidos de Serviço")
@RestController
@RequestMapping("/pedidos")
@SecurityRequirement(name = "bearerAuth")
public class PedidoServicoController {

    private final PedidoServicoService service;
    private final UsuarioRepository usuarioRepository;

    public PedidoServicoController(PedidoServicoService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    // 1. CRIAR (Usa email direto string)
    @PostMapping
    public ResponseEntity<PedidoServicoResponseDTO> criar(
            @RequestBody CriarPedidoServicoDTO dto,
            Principal principal
    ) {
        String emailCliente = principal.getName();
        return ResponseEntity.ok(service.criar(emailCliente, dto));
    }

    // 2. LISTAR (Usa o recuperador de ID)
    @GetMapping
    @Operation(summary = "Meus Pedidos", description = "Lista pedidos onde sou cliente ou prestador")
    public ResponseEntity<List<PedidoServicoResponseDTO>> listarMeusPedidos(Principal principal) {
        Long usuarioId = recuperarIdUsuario(principal);
        return ResponseEntity.ok(service.listarMeusPedidos(usuarioId));
    }

    // 3. MÉTODOS DE AÇÃO (CORRIGIDOS AGORA)
    // Antes estava Long.valueOf... Agora usa recuperarIdUsuario(principal)

    @PatchMapping("/{id}/aceitar")
    public ResponseEntity<Void> aceitar(@PathVariable Long id, Principal principal) {
        service.aceitar(id, recuperarIdUsuario(principal)); // <--- CORRIGIDO
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/recusar")
    public ResponseEntity<Void> recusar(@PathVariable Long id, Principal principal) {
        service.recusar(id, recuperarIdUsuario(principal)); // <--- CORRIGIDO
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Void> concluir(@PathVariable Long id, Principal principal) {
        service.concluir(id, recuperarIdUsuario(principal)); // <--- CORRIGIDO
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Principal principal) {
        service.cancelar(id, recuperarIdUsuario(principal)); // <--- CORRIGIDO
        return ResponseEntity.noContent().build();
    }

    // =======================================================
    // MÉTODO AUXILIAR
    // =======================================================
    private Long recuperarIdUsuario(Principal principal) {
        String email = principal.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado no banco de dados."));
        return usuario.getId();
    }
}