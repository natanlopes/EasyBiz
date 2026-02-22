package br.com.easybiz.service;

import org.springframework.stereotype.Service;

import br.com.easybiz.exception.ResourceNotFoundException;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository;

@Service
public class AuthContextService {

    private final UsuarioRepository usuarioRepository;

    public AuthContextService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Long getUsuarioIdByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado nao encontrado"));
    }
}
