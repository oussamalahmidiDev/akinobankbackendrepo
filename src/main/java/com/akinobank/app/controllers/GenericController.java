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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;


// controlleur generique qui peut etre utilisé par tt les utilisateurs.
@Controller
@CrossOrigin("*")
@RequiredArgsConstructor
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

    private final GoogleAuthenticator gAuth;

//    @PostMapping("/connect")
//    public String connect(HttpServletRequest request, Model model) {
//        String username = request.getParameter("username");
//        System.out.println(username);
//        return "views/admin/login";
//    }

    @PostMapping("/api/auth")
    @ResponseBody
    public ResponseEntity<?> authenticate (@RequestBody User user) throws Exception {
        System.out.println(user.getEmail() +" "+ user.getPassword());

        try {
            authService.authenticate(user.getEmail(), user.getPassword());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'email ou mot de passe est incorrect.");
        }

        Map<String, Object> response = new HashMap<>();

        User authenticatedUser = userRepository.findByEmail(user.getEmail());
        response.put("2fa_enabled", authenticatedUser.get_2FaEnabled());

        if (authenticatedUser.get_2FaEnabled()) {
            return ResponseEntity.ok(response);
        }

        final String token = jwtUtils.generateToken(authenticatedUser);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @SneakyThrows
    @GetMapping("/api/generate/{username}")
    public void generate(@PathVariable String username, HttpServletResponse response) {
        final GoogleAuthenticatorKey key = gAuth.createCredentials(username);

        //I've decided to generate QRCode on backend site
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        response.setContentType("image/png");

        String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL("akinobank", username, key);

        BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthURL, BarcodeFormat.QR_CODE, 200, 200);

        //Simple writing to outputstream
        ServletOutputStream outputStream = response.getOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);
        outputStream.close();
    }

    @PostMapping("/validate/key")
    @ResponseBody
    public HashMap<String, Boolean> validateKey(@RequestBody CodeValidationRequest body) {
        HashMap<String, Boolean> response = new HashMap<>();
//        response.put("valid", true);
        response.put("valid", gAuth.authorizeUser(body.getEmail(), body.getCode()));
        return response;
    }

    @PostMapping("/api/auth/code")
    @ResponseBody
    public ResponseEntity<?> validateAuthCode(@RequestBody CodeValidationRequest body) {
        HashMap<String, String> response = new HashMap<>();
        if (!gAuth.authorizeUser(body.getEmail(), body.getCode()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le code est invalide.");

        User authenticatedUser = userRepository.findByEmail(body.getEmail());
        final String token = jwtUtils.generateToken(authenticatedUser);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/agent")
    @ResponseBody
    public ResponseEntity<?> agentAuthenticate (@RequestBody User user) throws Exception {
        System.out.println(user.getEmail() +" "+ user.getPassword());

        try {
            Role role = userRepository.findByEmail(user.getEmail()).getRole();
            System.out.println(role);
            authService.agentAuthenticate(user.getEmail(), user.getPassword(),role);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'email ou mot de passe est incorrect");
        }

        final UserDetails userDetails = authService.loadUserByUsername(user.getEmail());

        final String token = jwtUtils.generateToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        System.out.println(jwtUtils.getAllClaimsFromToken(token));

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
            }
            else if (action.equals("confirm")) {
                if (userToVerify.isEmailConfirmed() && userToVerify.getPassword() != null)
                    return "redirect:/";

                userToVerify.setEmailConfirmed(true);
                userRepository.save(userToVerify);
                if (userToVerify.getPassword() != null)
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
