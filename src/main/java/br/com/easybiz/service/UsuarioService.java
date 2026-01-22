package br.com.easybiz.service;

import br.com.easybiz.dto.CriarUsuarioDTO;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario criarUsuario(CriarUsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(dto.nomeCompleto());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());
        

        return repository.save(usuario);
    }
}