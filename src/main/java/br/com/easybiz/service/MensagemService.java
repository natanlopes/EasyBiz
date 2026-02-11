package br.com.easybiz.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.easybiz.dto.MensagemResponseDTO;
import br.com.easybiz.model.Mensagem;
import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.MensagemRepository;
import br.com.easybiz.repository.PedidoServicoRepository;
import br.com.easybiz.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

@Service
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final PedidoServicoRepository pedidoServicoRepository;
    private final UsuarioRepository usuarioRepository;

    public MensagemService(
            MensagemRepository mensagemRepository,
            PedidoServicoRepository pedidoServicoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.mensagemRepository = mensagemRepository;
        this.pedidoServicoRepository = pedidoServicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public MensagemResponseDTO enviarMensagem(Long pedidoId, String emailRemetente, String conteudo) {
        PedidoServico pedido = buscarPedido(pedidoId);
        Usuario remetente = buscarUsuarioPorEmail(emailRemetente);
        validarParticipantePedido(pedido, remetente.getId());

        Mensagem mensagem = new Mensagem();
        mensagem.setPedidoServico(pedido);
        mensagem.setRemetente(remetente);
        mensagem.setConteudo(conteudo);
        mensagem.setEnviadoEm(LocalDateTime.now());
        mensagem.setLida(false);

        Mensagem salva = mensagemRepository.save(mensagem);

        return toResponseDTO(salva);
    }

    public List<MensagemResponseDTO> listarMensagens(Long pedidoId, String emailSolicitante) {
        PedidoServico pedido = buscarPedido(pedidoId);
        Long usuarioId = buscarUsuarioPorEmail(emailSolicitante).getId();
        validarParticipantePedido(pedido, usuarioId);

        return mensagemRepository
                .findByPedidoServico_IdOrderByEnviadoEmAsc(pedidoId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public void marcarComoLidas(Long pedidoId, String emailSolicitante) {
        PedidoServico pedido = buscarPedido(pedidoId);
        Long usuarioId = buscarUsuarioPorEmail(emailSolicitante).getId();
        validarParticipantePedido(pedido, usuarioId);

        List<Mensagem> mensagens = mensagemRepository.findNaoLidasDoPedido(pedidoId, usuarioId);

        for (Mensagem m : mensagens) {
            m.setLida(true);
            m.setLidaEm(LocalDateTime.now());
        }
    }

    @Transactional
    public void marcarMensagemEspecifica(Long pedidoId, Long mensagemId, String emailSolicitante) {
        PedidoServico pedido = buscarPedido(pedidoId);
        Long quemLeuId = buscarUsuarioPorEmail(emailSolicitante).getId();
        validarParticipantePedido(pedido, quemLeuId);

        Mensagem mensagem = mensagemRepository.findById(mensagemId)
                .orElseThrow(() -> new RuntimeException("Mensagem não encontrada"));

        if (!mensagem.getPedidoServico().getId().equals(pedidoId)) {
            throw new RuntimeException("Mensagem não pertence ao pedido");
        }

        if (mensagem.getRemetente().getId().equals(quemLeuId) || Boolean.TRUE.equals(mensagem.getLida())) {
            return;
        }

        mensagem.setLida(true);
        mensagem.setLidaEm(LocalDateTime.now());
    }

    public br.com.easybiz.dto.UltimoVistoDTO buscarUltimoVisto(Long pedidoId, String emailSolicitante) {
        PedidoServico pedido = buscarPedido(pedidoId);
        Long usuarioId = buscarUsuarioPorEmail(emailSolicitante).getId();
        validarParticipantePedido(pedido, usuarioId);

        LocalDateTime data = mensagemRepository.buscarUltimaLeitura(pedidoId, usuarioId);

        return new br.com.easybiz.dto.UltimoVistoDTO(pedidoId, data);
    }

    private PedidoServico buscarPedido(Long pedidoId) {
        return pedidoServicoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private void validarParticipantePedido(PedidoServico pedido, Long usuarioId) {
        Long idCliente = pedido.getCliente().getId();
        Long idProfissional = pedido.getNegocio().getUsuario().getId();
        boolean participante = idCliente.equals(usuarioId) || idProfissional.equals(usuarioId);

        if (!participante) {
            throw new SecurityException("Acesso negado: usuário não participa deste pedido.");
        }
    }

    private MensagemResponseDTO toResponseDTO(Mensagem mensagem) {
        return new MensagemResponseDTO(
                mensagem.getId(),
                mensagem.getPedidoServico().getId(),
                mensagem.getRemetente().getId(),
                mensagem.getRemetente().getNomeCompleto(),
                mensagem.getConteudo(),
                mensagem.getEnviadoEm(),
                mensagem.getLida(),
                mensagem.getLidaEm(),
                mensagem.getRemetente().getFotoUrl()
        );
    }
}
