package com.akinobank.app.controllers;

import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.enumerations.Role;
import com.akinobank.app.exceptions.ConfirmationPasswordException;
import com.akinobank.app.exceptions.InvalidVerificationTokenException;
import com.akinobank.app.models.CodeValidationRequest;
import com.akinobank.app.models.Compte;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.repositories.UserRepository;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.MailService;
import com.akinobank.app.utilities.JwtUtils;
import com.akinobank.app.utilities.VerificationTokenGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;


// controlleur generique qui peut etre utilisé par tt les utilisateurs.
@Controller
@RequiredArgsConstructor
@Log4j2
public class GenericController {

    Logger logger = LoggerFactory.getLogger(GenericController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private AuthService authService;


    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SecretGenerator secretGenerator;

    @Autowired
    private QrDataFactory qrDataFactory;

    @Autowired
    private CodeVerifier verifier;

    @Autowired
    private QrGenerator qrGenerator;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void index() {
        logger.info("PIING POOONG");
    }

    @PostMapping("/api/auth/agent")
    @ResponseBody
    public ResponseEntity<?> agentAuthenticate(@RequestBody User user) throws Exception {
        System.out.println(user.getEmail() + " " + user.getPassword());

        User authenticatedUser = userRepository.findByEmail(user.getEmail());
        try {
            Role role = authenticatedUser.getRole();
            System.out.println(role);
            authService.agentAuthenticate(user.getEmail(), user.getPassword(), role);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'email ou mot de passe est incorrect");
        }

        final String token = jwtUtils.generateToken(authenticatedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
//        System.out.println(jwtUtils.getAllClaimsFromToken(token));

        return ResponseEntity.ok(response);
    }

    // page de confirmation d email
    @GetMapping("/confirm")
    public String confirmEmail(HttpServletRequest request) {
        String token = request.getParameter("token");
        logger.info("Verif token : {}", token);
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
            } else if (action.equals("confirm")) {
                if (userToVerify.isEmailConfirmed()
                    && userToVerify.getPassword() != null
                    && userToVerify.get_2FaEnabled()
                )
                    return "redirect:/";

                userToVerify.setEmailConfirmed(true);
                userRepository.save(userToVerify);
                if (!userToVerify.get_2FaEnabled() && userToVerify.getPassword() != null)
                    return "redirect:/2fa_setup?token=" + token;

                if (userToVerify.getPassword() != null )
                    return "redirect:/";
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
        user.setPassword(encoder.encode(password));

        userRepository.save(user);

        if (!user.get_2FaEnabled())
            return "redirect:/2fa_setup?token=" + token;

        return "redirect:/";
    }

    @GetMapping("/2fa_setup")
    public String setup2FA(HttpServletRequest request, HttpServletResponse response, Model model) throws QrGenerationException {
        String token = request.getParameter("token");
        User user = userRepository.findOneByVerificationToken(token);
        if (user == null) throw new InvalidVerificationTokenException();

        if (user.get_2FaEnabled())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "2fa a été déja activée.");

        String email = user.getEmail();

        String secret = secretGenerator.generate();
        user.setSecretKey(secret);
        userRepository.save(user);

        log.info("QR secret key generated : {}", secret);
        QrData data = qrDataFactory.newBuilder()
            .label(email)
            .secret(secret)
            .issuer("Akinobank")
            .build();
        String qrCodeImage = getDataUriForImage(
            qrGenerator.generate(data),
            qrGenerator.getImageMimeType()
        );
        log.info("QR URI : {}", qrCodeImage);

        model.addAttribute("user", user);
        model.addAttribute("uri", qrCodeImage);
        model.addAttribute("secretkey", secret);

        return "views/2fa.set";
    }

    @PostMapping("/2fa_setup")
    @ResponseBody
    public HashMap<String, String> handleQRCode(HttpServletRequest request, @RequestBody CodeValidationRequest body) {
        String token = request.getParameter("token");
        User user = userRepository.findOneByVerificationToken(token);
        if (user == null) throw new InvalidVerificationTokenException();

        if (user.get_2FaEnabled())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "2fa a été déja activée.");

        String secretKey = user.getSecretKey();

        logger.info("Received code : {}", user.getSecretKey());

        if (!verifier.isValidCode(secretKey, body.getCode()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le code est invalide.");

        user.set_2FaEnabled(true);
        user.setVerificationToken(VerificationTokenGenerator.generateVerificationToken());
        userRepository.save(user);

        HashMap res = new HashMap<String, String>();
        res.put("message", "La verification à deux étapes a été activé avec succés");

        return res;
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
            Compte compte = compteRepository.findById(numeroCompte).get();
            if (compte.getStatut().name().equals(CompteStatus.PENDING_ACTIVE.name())) {
                compte.setStatut(CompteStatus.ACTIVE);
                compteRepository.save(compte);
            }

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
    @PostMapping("/verify")
    public ResponseEntity<String> sendVerification(@RequestBody User credentials) {
//        Long id = Long.parseLong(request.getParameter("id"));
        User user = userRepository.findByEmail(credentials.getEmail());
        mailService.sendVerificationMail(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
