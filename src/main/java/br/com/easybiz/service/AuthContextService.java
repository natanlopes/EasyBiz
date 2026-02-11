package br.com.easybiz.service;

import org.springframework.stereotype.Service;

import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository;

@Service
public class AuthContextService {

    private final UsuarioRepository usuarioRepository;

    public AuthContextService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Método principal usado nos Controllers para pegar o ID seguro
    public Long getUsuarioIdByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));
    }

    // Método extra caso precise do objeto Usuario completo em algum lugar
    public Usuario getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));
    }
}
