package com.quesssystems.sistemato.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
public class EmailUtil {
    @Value("${spring.mail.nome}")
    private String emailQuessNome;

    @Value("${spring.mail.username}")
    private String emailQuessEndereco;

    private final JavaMailSender mailSender;

    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmail(String destinatario, String assunto, String mensagem) throws MessagingException, UnsupportedEncodingException {
        MimeMessage email = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(email);

        helper.setFrom(emailQuessEndereco, emailQuessNome);
        helper.setTo(destinatario);
        helper.setSubject(assunto);
        helper.setText(mensagem, true);

        mailSender.send(email);
    }
}
