package com.akinobank.app.services;

import com.akinobank.app.models.User;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
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
    private String FROM;

    @Value("${mailgun.domain}")
    private String MAILGUN_DOMAIN;

    @Value("${mailgun.apikey}")
    private String MAILGUN_API_KEY;


    public void sendVerificationMail (User receiver, String url) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(FROM);
            messageHelper.setTo(receiver.getEmail());
            messageHelper.setSubject("Bienvenue " + receiver.getPrenom());

            Context context = new Context();
            context.setVariable("url", url);
            String content = templateEngine.process("mails/confirm", context);
            messageHelper.setText(content, true);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {}

    }

    public JsonNode sendVerificationMailViaMG (User receiver, String url) throws UnirestException {

        HttpResponse<JsonNode> request = Unirest.post("https://api.mailgun.net/v3/" + MAILGUN_DOMAIN + "/messages")
            .basicAuth("api", MAILGUN_API_KEY)
            .field("from", FROM)
            .field("to", receiver.getEmail())
            .field("subject", "Bienvenue " + receiver.getPrenom())
            .field("template", "email_verification")
            .field("v:url", url)
            .asJson();

        return request.getBody();
    }
}
