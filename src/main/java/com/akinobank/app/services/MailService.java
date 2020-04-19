package com.akinobank.app.services;

import com.akinobank.app.models.User;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

@Component
public class MailService  {

    @Autowired
    private HttpServletRequest request;

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


    public void sendVerificationMail (User receiver) {
        String rootURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        // generation du lien de confirmation et envoie par mail
        String confirmationURL = rootURL + "/confirm?token=" + receiver.getVerificationToken();

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(FROM);
            messageHelper.setTo(receiver.getEmail());
            messageHelper.setSubject("Bienvenue " + receiver.getPrenom());

            Context context = new Context();
            context.setVariable("url", confirmationURL);
            String content = templateEngine.process("mails/confirm", context);
            messageHelper.setText(content, true);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {}

    }

    public JsonNode sendVerificationMailViaMG (User receiver) throws UnirestException {
        String rootURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        // generation du lien de confirmation et envoie par mail
        String confirmationURL = rootURL + "/confirm?token=" + receiver.getVerificationToken();

        HttpResponse<JsonNode> request = Unirest.post("https://api.mailgun.net/v3/" + MAILGUN_DOMAIN + "/messages")
            .basicAuth("api", MAILGUN_API_KEY)
            .field("from", FROM)
            .field("to", receiver.getEmail())
            .field("subject", "Bienvenue " + receiver.getPrenom())
            .field("template", "email_verification")
            .field("v:url", confirmationURL)
            .asJson();

        return request.getBody();
    }
}
