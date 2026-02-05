package br.com.easybiz.repository;

import java.util.Optional; // <--- O IMPORT CORRETO (java.util)
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.easybiz.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    
}