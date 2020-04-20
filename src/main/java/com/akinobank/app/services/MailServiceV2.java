package com.akinobank.app.services;

import com.akinobank.app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

@Service
public class MailServiceV2 {


    @Autowired
    public HttpServletRequest request;

    @Autowired
    public JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String FROM;

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


    public void sendM(User receiver) {

        String rootURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        // generation du lien de confirmation et envoie par mail
        String confirmationURL = rootURL + "/confirm?token=" + receiver.getVerificationToken();


        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", HOST);
        properties.put("mail.smtp.port", PORT);
        properties.put("mail.smtp.ssl.enable", SMTP_SSL);
        properties.put("mail.smtp.auth", SMTP_AUTH);

        // Get the Session objec
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(USERNAME, PASSWORD);

            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(FROM));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver.getEmail()));

            // Set Subject: header field
            message.setSubject("Verification");

            // Now set the actual message;
            message.setText("Welcome in AkinoBank");

            MimeMessageHelper messageHelper = new MimeMessageHelper(message);
            messageHelper.setSubject("Bienvenue " + receiver.getPrenom());
            Context context = new Context();
            context.setVariable("url", confirmationURL);
            String content = templateEngine.process("mails/confirm", context);
            messageHelper.setText(content, true);


            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

    }
}
