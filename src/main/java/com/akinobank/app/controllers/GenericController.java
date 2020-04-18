package com.akinobank.app.controllers;

import com.akinobank.app.exceptions.ConfirmationPasswordException;
import com.akinobank.app.exceptions.InvalidVerificationTokenException;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.UserRepository;
import com.akinobank.app.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;


// controlleur generique qui peut etre utilisé par tt les utilisateurs.
@Controller
public class GenericController {

    Logger logger = LoggerFactory.getLogger(GenericController.class);

    @Autowired
    private UserRepository userRepository;

    // page de confirmation d email
    @GetMapping("/confirm")
    public String confirmEmail(HttpServletRequest request) {
        String token = request.getParameter("token");
        logger.info("Verif token", token);
        User userToVerify = userRepository.findOneByVerificationToken(token);
        if (userToVerify == null) throw new InvalidVerificationTokenException();
        userToVerify.setEmailConfirmed(true);
        // generation d'un nouveau token pour revoker l'ancien token
        token = (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replace("-", "");
        userToVerify.setVerificationToken(token);
        userRepository.save(userToVerify);
        return "redirect:/set_password?token=" + token;
    }

    // page de config de password
    @GetMapping("/set_password")
    public String setPasswordView(HttpServletRequest request, Model model) {
        String token = request.getParameter("token");
        User user = userRepository.findOneByVerificationToken(token);
        if (user == null) throw new InvalidVerificationTokenException();

        model.addAttribute("user", user);
        return "views/password.set";
    }
    @PostMapping("/set_password")
    public String setPassword(HttpServletRequest request) {
        String token = request.getParameter("token");
        User user = userRepository.findOneByVerificationToken(token);
        if (user == null) throw new InvalidVerificationTokenException();
        String password = request.getParameter("password");
        String passwordConfirmation = request.getParameter("password_conf");
        if (!password.equals(passwordConfirmation)) throw new ConfirmationPasswordException();
        user.setPassword(password);

        userRepository.save(user);

        return "redirect:/";
    }



}
