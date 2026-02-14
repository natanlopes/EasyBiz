package br.com.easybiz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:noreply@easybiz.com.br}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailRecuperacao(String destinatario, String token) {
        String assunto = "EasyBiz - Codigo de Recuperacao de Senha";
        String corpo = String.format(
            "Ola!\n\n"
            + "Voce solicitou a recuperacao de senha no EasyBiz.\n\n"
            + "Seu codigo de verificacao e: %s\n\n"
            + "Este codigo expira em 15 minutos.\n\n"
            + "Se voce nao solicitou esta recuperacao, ignore este e-mail.\n\n"
            + "Equipe EasyBiz",
            token
        );

        if (mailEnabled) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetente);
            message.setTo(destinatario);
            message.setSubject(assunto);
            message.setText(corpo);
            mailSender.send(message);
            log.info("E-mail de recuperacao enviado para: {}", destinatario);
        } else {
            log.info("========================================");
            log.info("MODO DEV - E-mail de recuperacao de senha");
            log.info("Para: {}", destinatario);
            log.info("Codigo: {}", token);
            log.info("========================================");
        }
    }
}
