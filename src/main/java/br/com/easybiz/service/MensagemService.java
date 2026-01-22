package br.com.easybiz.service;

import java.util.List;
import org.springframework.stereotype.Service;
import br.com.easybiz.dto.EnviarMensagemDTO;
import br.com.easybiz.model.Mensagem;
import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.MensagemRepository;
import br.com.easybiz.repository.PedidoServicoRepository;
import br.com.easybiz.repository.UsuarioRepository;

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

    public Mensagem enviarMensagem(Long pedidoId, EnviarMensagemDTO dto) {
        PedidoServico pedido = pedidoServicoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        Usuario remetente = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Mensagem mensagem = new Mensagem();
        
        mensagem.setPedidoServico(pedido); 
        mensagem.setRemetente(remetente);
        mensagem.setConteudo(dto.conteudo());
       

        return mensagemRepository.save(mensagem);
    }

    public List<Mensagem> listarMensagens(Long pedidoId) {
      
        return mensagemRepository.findByPedidoServico_IdOrderByEnviadoEmAsc(pedidoId);
    }
}