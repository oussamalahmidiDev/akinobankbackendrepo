package com.akinobank.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class MailService  {

    @Autowired
    public JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${mail.from}")
    private String from;

    public void send (String to, String subject, String text) {

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);

            Context context = new Context();
            context.setVariable("url", text);
            String content = templateEngine.process("mails/confirm", context);
            messageHelper.setText(content, true);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {}

    }
}
