package br.com.easybiz.service;

import br.com.easybiz.model.Negocio;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.NegocioRepository;
import br.com.easybiz.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class NegocioService {

    private NegocioRepository negocioRepository;
    private UsuarioRepository usuarioRepository;

    public NegocioService(NegocioRepository negocioRepository,
                          UsuarioRepository usuarioRepository) {
        this.negocioRepository = negocioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Negocio criarNegocio(Long usuarioId, String nome, String tipo) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Negocio negocio = new Negocio();
        negocio.setNome(nome);
        negocio.setTipo(tipo);
        negocio.setUsuario(usuario);
        negocio.setAtivo(true);

        return negocioRepository.save(negocio);
    }
}

