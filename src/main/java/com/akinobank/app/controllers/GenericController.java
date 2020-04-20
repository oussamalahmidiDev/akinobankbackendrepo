package com.akinobank.app.controllers;

import com.akinobank.app.exceptions.ConfirmationPasswordException;
import com.akinobank.app.exceptions.InvalidVerificationTokenException;
import com.akinobank.app.models.Compte;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.repositories.UserRepository;
import com.akinobank.app.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.UUID;


// controlleur generique qui peut etre utilisé par tt les utilisateurs.
@Controller
public class GenericController {

    Logger logger = LoggerFactory.getLogger(GenericController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private MailService mailService;

    // page de confirmation d email
    @GetMapping("/confirm")
    public String confirmEmail(HttpServletRequest request) {
        String token = request.getParameter("token");
        logger.info("Verif token", token);
        User userToVerify = userRepository.findOneByVerificationToken(token);
        if (userToVerify == null) throw new InvalidVerificationTokenException();
        String action = request.getParameter("action");
        String ccn = request.getParameter("ccn");
        try {
            if (action.equals("compte_details")) {
                if (!userToVerify.isEmailConfirmed())
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Veuillez verifier votre email.");
                if (ccn == null || ccn == "")
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

                return "redirect:/compte_details?ref=email&token=" + token + "&ccn=" + ccn;
            }
            else if (action.equals("confirm")) {
                if (userToVerify.isEmailConfirmed() && userToVerify.getPassword() != "")
                    return "redirect:/";

                userToVerify.setEmailConfirmed(true);
                userRepository.save(userToVerify);
                return "redirect:/set_password?token=" + token;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
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

    @GetMapping("/compte_details")
    public String getCompteDetailsView(HttpServletRequest request, Model model) {
        String token = request.getParameter("token");
        String ccn = request.getParameter("ccn");
        User user = userRepository.findOneByVerificationToken(token);
        if (user == null) throw new InvalidVerificationTokenException();
        if (ccn == null || ccn == "")
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        model.addAttribute("user", user);
        model.addAttribute("ccn", ccn);
        return "views/compte_details";
    }

    @PostMapping("/compte_details")
    @ResponseBody
    public HashMap<String, Object> getCompteDetails(@RequestBody HashMap<String, String> request) {
        String token = request.get("token");
        String numeroCompte = request.get("ccn");

        logger.info("TOKEN = " + token);

        User user = userRepository.findOneByVerificationToken(token);
        if (user == null) throw new InvalidVerificationTokenException();

        try {
            Compte compte =  compteRepository.findById(numeroCompte).get();
            HashMap<String, Object> response = new HashMap<>();

            response.put("numeroCompte", compte.getNumeroCompteHidden());
            response.put("codeSecret", compte.getCodeSecret());
            response.put("intitule", compte.getIntitule());
            response.put("solde", compte.getSolde());
            response.put("status", compte.getStatut());
            response.put("ajoute_le", compte.getDateDeCreation());
            response.put("ajoute_par", compte.getClient().getAgent().getUser().getPrenom() + " " + compte.getClient().getAgent().getUser().getNom());

            return response;
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro est invalide.");
        }
    }

    @ResponseBody
    @GetMapping("/verify")
    public ResponseEntity<String> sendVerification(HttpServletRequest request) {
        Long id = Long.parseLong(request.getParameter("id"));
        User user = userRepository.getOne(id);
        mailService.sendVerificationMail(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }



}
