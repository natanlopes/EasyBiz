package br.com.easybiz.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.easybiz.exception.BusinessException;
import br.com.easybiz.model.PasswordResetToken;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.PasswordResetTokenRepository;
import br.com.easybiz.repository.UsuarioRepository;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_EXPIRATION_MINUTES = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(
            UsuarioRepository usuarioRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void solicitarReset(String email) {
        var usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            log.debug("Tentativa de reset para e-mail nao cadastrado: {}", email);
            return; // Nao revela se o e-mail existe
        }

        Usuario usuario = usuarioOpt.get();

        // Invalida tokens anteriores
        List<PasswordResetToken> tokensAnteriores = tokenRepository.findByUsuarioAndUsadoFalse(usuario);
        tokensAnteriores.forEach(t -> t.setUsado(true));
        tokenRepository.saveAll(tokensAnteriores);

        // Gera codigo de 6 digitos
        String codigo = gerarCodigo6Digitos();

        // Salva token no banco
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .usuario(usuario)
                .token(codigo)
                .expiracao(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES))
                .build();
        tokenRepository.save(resetToken);

        // Envia e-mail (ou loga no console em dev)
        emailService.enviarEmailRecuperacao(email, codigo);

        log.info("Token de reset gerado para usuario ID: {}", usuario.getId());
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsadoFalse(token)
                .orElseThrow(() -> new BusinessException("Codigo invalido ou ja utilizado"));

        if (resetToken.isExpirado()) {
            throw new BusinessException("Codigo expirado. Solicite um novo codigo");
        }

        // Atualiza a senha
        Usuario usuario = resetToken.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        // Marca token como usado
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        log.info("Senha redefinida com sucesso para usuario ID: {}", usuario.getId());
    }

    private String gerarCodigo6Digitos() {
        int codigo = SECURE_RANDOM.nextInt(900000) + 100000; // 100000 a 999999
        return String.valueOf(codigo);
    }
}
