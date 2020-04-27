package com.akinobank.app.services;

import com.akinobank.app.models.Compte;
import com.akinobank.app.models.User;
import com.akinobank.app.models.Virement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

//import org.springframework.mail.javamail.JavaMailSender;

@Component
public class MailService  {

    @Autowired
    private HttpServletRequest request;

    private Logger logger = LoggerFactory.getLogger(MailService.class);

//    @Autowired
//    public JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${mail.from}")
    private String FROM;

    @Value("${mailgun.domain}")
    private String MAILGUN_DOMAIN;

    @Value("${mailgun.apikey}")
    private String MAILGUN_API_KEY;

    @Value("${spring.mail.host}")
    private String HOST;

    @Value("${spring.mail.port}")
    private String PORT;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean SMTP_AUTH;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable}")
    private boolean SMTP_SSL;

    @Value("${spring.mail.username}")
    private String USERNAME;

    @Value("${spring.mail.password}")
    private String PASSWORD;

    public void sendCompteDetails (User receiver, Compte compte) {
        String url = getConfirmationURL(receiver) + "&action=compte_details&ccn=" + compte.getNumeroCompte().substring(12);
        try {
            MimeMessage message = getMimeMessage(
                receiver.getEmail(),
                "Details de votre compte AKINOBANK",
                "Bienvenue "  + receiver.getPrenom() + " sur AKINOBANK"
            );
            MimeMessageHelper messageHelper = new MimeMessageHelper(message);

            Context context = new Context();
            context.setVariable("receiver", receiver);
            context.setVariable("compte", compte);
            context.setVariable("url", url);
            String content = templateEngine.process("mails/compte_details", context);
            messageHelper.setText(content, true);
            // Send message
            Transport.send(message);
            logger.info("Compte details sent to " + receiver.getEmail() + " successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    public void sendVerificationMail (User receiver) {
        String url = getConfirmationURL(receiver) + "&action=confirm";
        try {
            MimeMessage message = getMimeMessage(
                receiver.getEmail(),
                "Verification d'email",
                "Bienvenue "  + receiver.getPrenom() + " sur AKINOBANK"
            );
            MimeMessageHelper messageHelper = new MimeMessageHelper(message);

            Context context = new Context();
            context.setVariable("url", url);
            context.setVariable("receiver", receiver);
            String content = templateEngine.process("mails/confirm", context);
            messageHelper.setText(content, true);
            // Send message
            Transport.send(message);
            logger.info("Message sent successfully.");
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }

    public void sendVirementCodeMail (User receiver, Virement virement) {
        try {
            MimeMessage message = getMimeMessage(
                receiver.getEmail(),
                "Code de verification de virement",
                "Bienvenue "  + receiver.getPrenom()
            );
            MimeMessageHelper messageHelper = new MimeMessageHelper(message);

            Context context = new Context();
            context.setVariable("virement", virement);
            context.setVariable("receiver", receiver);
            String content = templateEngine.process("mails/virement", context);
            messageHelper.setText(content, true);
            // Send message
            Transport.send(message);
            logger.info("Virment Message sent successfully.");
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }
    private MimeMessage getMimeMessage(String to, String subject, String text) throws MessagingException {
        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(getMailSession());

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(FROM));

        // Set To: header field of the header.
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

        // Set Subject: header field
        message.setSubject(subject);

        // Now set the actual message;
        message.setText(text);
        return message;
    }


    private Session getMailSession() {
        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", HOST);
        properties.put("mail.smtp.port", PORT);
        properties.put("mail.smtp.ssl.enable", SMTP_SSL);
        properties.put("mail.smtp.auth", SMTP_AUTH);
        properties.put("mail.mime.charset", "UTF-8");

        // Get the Session objec
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }

        });
        // Used to debug SMTP issues
        session.setDebug(true);
        return session;
    }

    private String getConfirmationURL(User receiver) {
        String rootURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        // generation du lien de confirmation et envoie par mail
        return rootURL + "/confirm?token=" + receiver.getVerificationToken();
    }


//    public void sendVerificationMailViaDefault (User receiver) {
//        String rootURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
//        // generation du lien de confirmation et envoie par mail
//        String confirmationURL = rootURL + "/confirm?token=" + receiver.getVerificationToken();
//
//        MimeMessagePreparator messagePreparator = mimeMessage -> {
//            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
//            messageHelper.setFrom(FROM);
//            messageHelper.setTo(receiver.getEmail());
//            messageHelper.setSubject("Bienvenue " + receiver.getPrenom());
//
//            Context context = new Context();
//            context.setVariable("url", confirmationURL);
//            String content = templateEngine.process("mails/confirm", context);
//            messageHelper.setText(content, true);
//        };
//        try {
//            javaMailSender.send(messagePreparator);
//        } catch (MailException e) {}
//
//    }

//    public JsonNode sendVerificationMailViaMG (User receiver) throws UnirestException {
//        String rootURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
//        // generation du lien de confirmation et envoie par mail
//        String confirmationURL = rootURL + "/confirm?token=" + receiver.getVerificationToken();
//
//        HttpResponse<JsonNode> request = Unirest.post("https://api.mailgun.net/v3/" + MAILGUN_DOMAIN + "/messages")
//            .basicAuth("api", MAILGUN_API_KEY)
//            .field("from", FROM)
//            .field("to", receiver.getEmail())
//            .field("subject", "Bienvenue " + receiver.getPrenom())
//            .field("template", "email_verification")
//            .field("v:url", confirmationURL)
//            .asJson();
//
//        logger.info(request.getStatusText());
//        logger.info(String.valueOf(request.getBody()));
//
//        return request.getBody();
//    }
}
