package com.signflow.application.service.impl;

import com.signflow.application.service.EmailService;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendMfaCode(UserEntity user, String code) {
        log.info("Enviando código MFA por e-mail para o usuário: {}", user.getUsername());
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@signflow.api.br");
        message.setTo(user.getEmail());
        message.setSubject("Seu código de acesso SignFlow");
        message.setText("Olá " + user.getName() + ",\n\n" +
                "Seu código de autenticação para acesso ao Signflow é: " + code + "\n\n" +
                "Este código expira em 5 minutos.\n" +
                "Se você não solicitou este código, ignore este e-mail.");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de MFA para {}: {}", user.getEmail(), e.getMessage());
            // Em produção, poderíamos lançar uma exceção ou tratar de outra forma.
            // Para este desafio, apenas logamos.
        }
    }
}
