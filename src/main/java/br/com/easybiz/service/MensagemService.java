package br.com.easybiz.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import br.com.easybiz.dto.EnviarMensagemDTO;
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

 // 🔹 AGORA retorna DTO
    public MensagemResponseDTO enviarMensagem(Long pedidoId, EnviarMensagemDTO dto) {

        PedidoServico pedido = pedidoServicoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        Usuario remetente = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Mensagem mensagem = new Mensagem();
        mensagem.setPedidoServico(pedido);
        mensagem.setRemetente(remetente);
        mensagem.setConteudo(dto.conteudo());

        Mensagem salva = mensagemRepository.save(mensagem);

        return toResponseDTO(salva);
    }

    // 🔹 AGORA retorna lista de DTO
    public List<MensagemResponseDTO> listarMensagens(Long pedidoId) {

        return mensagemRepository
                .findByPedidoServico_IdOrderByEnviadoEmAsc(pedidoId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 🔹 Mapper centralizado
    private MensagemResponseDTO toResponseDTO(Mensagem mensagem) {
        return new MensagemResponseDTO(
                mensagem.getId(),
                mensagem.getPedidoServico().getId(),
                mensagem.getRemetente().getId(),
                mensagem.getRemetente().getNomeCompleto(),
                mensagem.getConteudo(),
                mensagem.getEnviadoEm(),
                mensagem.getLida(),
                mensagem.getLidaEm()
        );
    }

    @Transactional
    public void marcarComoLidas(Long pedidoId, Long usuarioId) {

        List<Mensagem> mensagens = mensagemRepository
                .findNaoLidasDoPedido(pedidoId, usuarioId);

        for (Mensagem m : mensagens) {
            m.setLida(true);
            m.setLidaEm(LocalDateTime.now());
        }

        mensagemRepository.saveAll(mensagens);
    }

	@Transactional
	public void marcarMensagemEspecifica(Long pedidoId, Long mensagemId, Long quemLeuId) {
		Mensagem mensagem = mensagemRepository.findById(mensagemId)
				.orElseThrow(() -> new RuntimeException("Mensagem não encontrada"));

		if (!mensagem.getPedidoServico().getId().equals(pedidoId)) {
			throw new RuntimeException("Mensagem não pertence ao pedido");
		}

		if (mensagem.getRemetente().getId().equals(quemLeuId)) {
			return; // remetente não marca própria mensagem como lida
		}

		if (mensagem.getLida()) {
			return;
		}

		mensagem.setLida(true);
		mensagem.setLidaEm(LocalDateTime.now());
	}


    public br.com.easybiz.dto.UltimoVistoDTO buscarUltimoVisto(Long pedidoId, Long usuarioId) {
        // Busca no banco
        LocalDateTime data = mensagemRepository.buscarUltimaLeitura(pedidoId, usuarioId);
        
        // Retorna o DTO
        return new br.com.easybiz.dto.UltimoVistoDTO(pedidoId, data);
    }


}