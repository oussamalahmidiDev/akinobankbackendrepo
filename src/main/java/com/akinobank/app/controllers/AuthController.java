package com.akinobank.app.controllers;

import com.akinobank.app.models.CodeValidationRequest;
import com.akinobank.app.models.Session;
import com.akinobank.app.models.TokenResponse;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.SessionRepository;
import com.akinobank.app.repositories.UserRepository;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.utilities.JwtUtils;
import com.akinobank.app.utilities.SessionsUtils;
import com.maxmind.geoip2.model.CityResponse;
import dev.samstevens.totp.code.CodeVerifier;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Log4j2
public class AuthController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CodeVerifier verifier;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionsUtils sessionsUtils;

    @PostMapping("")
    public ResponseEntity<?> authenticate(@RequestBody User user, HttpServletResponse response, HttpServletRequest request) throws Exception {
        System.out.println(user.getEmail() + " " + user.getPassword());

        //      Step 1: Verify credentials
        try {
            authService.authenticate(user.getEmail(), user.getPassword());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'email ou mot de passe est incorrect.");
        }

        TokenResponse responseBody = new TokenResponse();

        //        Step 2: Check if user has 2fa enabled
        User authenticatedUser = userRepository.findByEmail(user.getEmail());
        responseBody.set_2FaEnabled(authenticatedUser.get_2FaEnabled());
        if (authenticatedUser.get_2FaEnabled()) {
            return ResponseEntity.ok(responseBody);
        }

        //        Step 3: Generate a jwt token [case 2fa is not enabled]
        final String token = jwtUtils.generateToken(authenticatedUser);
        responseBody.setToken(token);
        responseBody.setExpireAt(jwtUtils.getExpirationDateFromToken(token));

        //        Step 4: Register a session in DB for the authenticated user
        CityResponse cityResponse = sessionsUtils.getCityResponse();
        Session session = Session.builder()
            .browser(sessionsUtils.getUserAgent().getBrowser().getName())
            .ip(sessionsUtils.getIpAdress())
            .ville(cityResponse != null? cityResponse.getCity().getName() : null)
            .pays(cityResponse != null? cityResponse.getCountry().getName() : null)
            .operatingSystem(sessionsUtils.getUserAgent().getOperatingSystem().getName())
            .user(authenticatedUser)
            .authorized(authenticatedUser.getSessions().isEmpty())
            .build();
        sessionRepository.save(session);

//        Step 5: Send refresh token and session id cookies in response.
        Cookie refreshTokenCookie = new Cookie("refresh_token", session.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        Cookie sessionIDCookie = new Cookie("session_id", session.getId());
        sessionIDCookie.setPath("/");
        response.addCookie(sessionIDCookie);

        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> getNewToken(@CookieValue(value = "refresh_token", defaultValue = "") String refreshToken) {
        log.info("Received refresh token : {}", refreshToken);

        Session session = sessionRepository.findByRefreshToken(refreshToken).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh token.")
        );

//        AAAAAAAW ... jit mseht lcookies sdeqt mbloque rassi bera HHHHHHH
//        if (!session.getAuthorized())
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session is not yet authorized.");


        User authenticatedUser = session.getUser();

        TokenResponse response = new TokenResponse();

        final String token = jwtUtils.generateToken(authenticatedUser);
        response.setToken(token);
        response.setExpireAt(jwtUtils.getExpirationDateFromToken(token));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> handleLogout(@CookieValue(value = "refresh_token", defaultValue = "") String refreshToken) {

        Session session = sessionRepository.findByRefreshToken(refreshToken).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh token.")
        );
        sessionRepository.delete(session);

        return ResponseEntity.ok("");
    }

    @PostMapping("/code")
    public ResponseEntity<?> validateAuthCode(@RequestBody CodeValidationRequest body, HttpServletRequest request, HttpServletResponse response) {
        //      Step 1: Verify credentials
        try {
            authService.authenticate(body.getEmail(), body.getPassword());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials.");
        }

        //      Step 2: Check if user has truely activaed 2fa
        User authenticatedUser = userRepository.findByEmail(body.getEmail());
        if (!authenticatedUser.get_2FaEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        //      Step 3: Validate 2fa code
        if (!verifier.isValidCode(authenticatedUser.getSecretKey(), body.getCode()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le code est invalide.");

        //      Step 4: Generate a jwt token
        final String token = jwtUtils.generateToken(authenticatedUser);
        TokenResponse responseBody = new TokenResponse();
        responseBody.setToken(token);
        responseBody.setExpireAt(jwtUtils.getExpirationDateFromToken(token));

        //      Step 5: Generate a refresh token and send it in a cookie
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        authenticatedUser.setRefreshToken(refreshToken);
        userRepository.save(authenticatedUser);

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/sessions")
    public List<Session> getSessions() {
        return sessionRepository.findAllByUser(authService.getCurrentUser());
    }

    @GetMapping("/sessions/{id}")
    public Session getSessionById (@PathVariable String id) {
        return sessionRepository.findByIdAndUser(id, authService.getCurrentUser()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
    }

    @PutMapping("/sessions/{id}/{action}")
    public Session setSessionState(@PathVariable String id, @PathVariable("action") String action) {
        Session session = getSessionById(id);
        log.info("Chosen action : {}", action);

        if (action.equals("authorize")) {
            session.setAuthorized(true);
            sessionRepository.save(session);
        }
        else if (action.equals("block")) {
            deleteSession(id);
        }
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action is undefined");

        return session;
    }

    @DeleteMapping("/sessions/{id}/delete")
    public ResponseEntity deleteSession (@PathVariable String id) {
        sessionRepository.delete(getSessionById(id));
        return ResponseEntity.ok("");
    }

}
