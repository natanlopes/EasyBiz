package br.com.easybiz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.easybiz.model.PasswordResetToken;
import br.com.easybiz.model.Usuario;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsadoFalse(String token);

    List<PasswordResetToken> findByUsuarioAndUsadoFalse(Usuario usuario);
}
