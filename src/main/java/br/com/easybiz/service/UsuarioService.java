package br.com.easybiz.service;

import org.springframework.stereotype.Service;

import br.com.easybiz.dto.CriarUsuarioDTO;
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
              throw new RuntimeException("Email já cadastrado");
          }
        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(dto.nomeCompleto());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha())); //  HASH AQUI


        return repository.save(usuario);
    }
    // 🆕 Método para atualizar foto
    public void atualizarFoto(Long usuarioId, String novaUrl) {
        Usuario usuario = repository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        usuario.setFotoUrl(novaUrl);
        repository.save(usuario);
    }
}