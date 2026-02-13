package br.com.easybiz.service;

import org.springframework.stereotype.Service;

import br.com.easybiz.dto.CriarUsuarioDTO;
import br.com.easybiz.exception.BusinessException;
import br.com.easybiz.exception.ResourceNotFoundException;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario criarUsuario(CriarUsuarioDTO dto) {
        if (repository.findByEmail(dto.email()).isPresent()) {
            throw new BusinessException("Email ja cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(dto.nomeCompleto());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));

        return repository.save(usuario);
    }

    public void atualizarFoto(Long usuarioId, String novaUrl) {
        Usuario usuario = repository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        usuario.setFotoUrl(novaUrl);
        repository.save(usuario);
    }
}
