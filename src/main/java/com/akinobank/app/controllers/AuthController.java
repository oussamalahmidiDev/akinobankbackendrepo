package com.akinobank.app.controllers;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.CodeValidationRequest;
import com.akinobank.app.models.Session;
import com.akinobank.app.models.TokenResponse;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.SessionRedisRepository;
import com.akinobank.app.repositories.UserRepository;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.SessionService;
import com.akinobank.app.utilities.JwtUtils;
import dev.samstevens.totp.code.CodeVerifier;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.NoSuchElementException;

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
    private SessionRedisRepository sessionRedisRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ActivitiesService activitiesService;

    @PostMapping("")
    public ResponseEntity<?> authenticate(@CookieValue(value = "session_id", defaultValue = "") String sessionId, @RequestBody User user, HttpServletResponse response) {
        User authenticatedUser = authService.authenticate(user.getEmail(), user.getPassword());
        TokenResponse responseBody = new TokenResponse();

        Session session = sessionRedisRepository.findByIdAndUserId(sessionId, authenticatedUser.getId());
        log.info("Session exists : {} of id : {}", session != null, sessionId);

        if (authenticatedUser.getRole().equals(Role.CLIENT)) {
            try {
                if (!session.getAuthorized()) {
                    responseBody.set_2FaEnabled(!session.getAuthorized());
                    return ResponseEntity.status(HttpStatus.OK).body(responseBody);
                }
                response.addCookie(sessionService.buildCookie("refresh_token", sessionService.generateSession(authenticatedUser, session, sessionId).getRefreshToken(), "/", true));
            } catch (NoSuchElementException | NullPointerException e) {
                log.info("Detected a new session");
                responseBody.set_2FaEnabled(true);
                return ResponseEntity.status(HttpStatus.OK).body(responseBody);
            }
        } else {
            Session newSession = sessionService.generateSession(authenticatedUser, session, sessionId);
            response.addCookie(sessionService.buildCookie("session_id", newSession.getId(), "/", false));
            response.addCookie(sessionService.buildCookie("refresh_token", newSession.getRefreshToken(), "/", true));
        }

        //        Step 3: Generate a jwt token [case 2fa is not enabled on this session]
        final String token = jwtUtils.generateToken(authenticatedUser);
        responseBody.setToken(token);

//
//        if (!authenticatedUser.getRole().equals(Role.CLIENT)) {
//
//        } else {
////      Step 4: Generate session data and send it in a cookie
//        }

        activitiesService.save(
            String.format("Authentification de %s %s", authenticatedUser.getPrenom(), authenticatedUser.getNom()),
            ActivityCategory.AUTH,
            authenticatedUser
        );

        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> getNewToken(@CookieValue(value = "session_id", defaultValue = "") String sessionId, @CookieValue(value = "refresh_token", defaultValue = "") String refreshToken) {
        log.info("Received session : {} refresh token : {}", sessionId, refreshToken);

        Session session = sessionRedisRepository.findById(sessionId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid session ID.")
        );

        if (!session.getRefreshToken().equals(refreshToken))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh token.");

        User authenticatedUser = userRepository.getOne(session.getUserId());

        TokenResponse response = new TokenResponse();

        final String token = jwtUtils.generateToken(authenticatedUser);
        response.setToken(token);
        response.setExpireAt(jwtUtils.getExpirationDateFromToken(token));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void handleLogout(@CookieValue(value = "session_id", defaultValue = "") String sessionId) {

        Session session = sessionRedisRepository.findById(sessionId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid session ID.")
        );

        session.setRefreshToken(null);
        sessionRedisRepository.save(session);

    }

    @PostMapping("/code")
    public ResponseEntity<?> validateAuthCode(@CookieValue(value = "session_id", defaultValue = "") String sessionId, @RequestBody CodeValidationRequest body, HttpServletRequest request, HttpServletResponse response) {
        //      Step 1: Verify credentials again
        User authenticatedUser = authService.authenticate(body.getEmail(), body.getPassword());

        //      Step 2: Validate 2fa code
        if (!verifier.isValidCode(authenticatedUser.getSecretKey(), body.getCode()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est invalide.");

        Session session = sessionService.generateSession(authenticatedUser, sessionRedisRepository.findByIdAndUserId(sessionId, authenticatedUser.getId()), sessionId);

        //      Step 3: Generate a jwt token
        final String token = jwtUtils.generateToken(authenticatedUser);
        TokenResponse responseBody = new TokenResponse();
        responseBody.setToken(token);

        //      Step 4: Generate session data and send it in a cookie
        response.addCookie(sessionService.buildCookie("refresh_token", session.getRefreshToken(), "/", true));
        response.addCookie(sessionService.buildCookie("session_id", session.getId(), "/", false));

        activitiesService.save(
            String.format("Authentification de %s %s", authenticatedUser.getPrenom(), authenticatedUser.getNom()),
            ActivityCategory.AUTH,
            authenticatedUser
        );


        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/sessions")
    public Iterable<Session> getSessions() {
        return sessionRedisRepository.findAllByUserId(authService.getCurrentUser().getId());
    }

    @GetMapping("/sessions/{id}")
    public Session getSessionById(@PathVariable String id) {
        Session session = sessionRedisRepository.findByIdAndUserId(id, authService.getCurrentUser().getId());
        if (session == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found.");

        return sessionRedisRepository.findByIdAndUserId(id, authService.getCurrentUser().getId());
    }

    @PutMapping("/sessions/{id}/{action}")
    public Session setSessionState(@PathVariable String id, @PathVariable("action") String action) {
        Session session = getSessionById(id);
        log.info("Chosen action : {}", action);

        if (action.equals("authorize")) {
            session.setAuthorized(true);
            sessionRedisRepository.save(session);
            activitiesService.save(
                String.format("Désactivation de l'authentification à 2 facteurs pour l'appareil definitive de %s", session.getOperatingSystem()),
                ActivityCategory.SESSIONS_AUTHORIZE
            );

        } else if (action.equals("block")) {
            session.setAuthorized(false);
            sessionRedisRepository.save(session);
            activitiesService.save(
                String.format("Activation de l'authentification à 2 facteurs pour l'appareil definitive de %s", session.getOperatingSystem()),
                ActivityCategory.SESSIONS_BLOCK
            );
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action {" + action + "} is undefined");

        return session;
    }

    @DeleteMapping("/sessions/{id}/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSession(@PathVariable String id) {
        Session session = getSessionById(id);
        sessionRedisRepository.delete(session);
        activitiesService.save(
            String.format("Suppression definitive de la session de %s, %s (%s)", session.getBrowser(), session.getOperatingSystem(), session.getId()),
            ActivityCategory.SESSIONS_D
        );
    }

}
