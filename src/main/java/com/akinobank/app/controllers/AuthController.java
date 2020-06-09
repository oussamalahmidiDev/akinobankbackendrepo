package com.akinobank.app.controllers;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.CodeValidationRequest;
import com.akinobank.app.models.Session;
import com.akinobank.app.models.TokenResponse;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.SessionRedisRepository;
import com.akinobank.app.repositories.UserRepository;
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
            } catch (NoSuchElementException | NullPointerException e) {
                log.info("Detected a new session");
                responseBody.set_2FaEnabled(true);
                return ResponseEntity.status(HttpStatus.OK).body(responseBody);
            }

        }
        //        Step 3: Generate a jwt token [case 2fa is not enabled on this session]
        final String token = jwtUtils.generateToken(authenticatedUser);
        responseBody.setToken(token);

        Session newSession = sessionService.generateSession(authenticatedUser, session, sessionId);

        if (!authenticatedUser.getRole().equals(Role.CLIENT)) {
            response.addCookie(sessionService.buildCookie("session_id", newSession.getId(), "/", false));
            response.addCookie(sessionService.buildCookie("refresh_token", newSession.getRefreshToken(), "/", true));

        } else {
//      Step 4: Generate session data and send it in a cookie
            response.addCookie(sessionService.buildCookie("refresh_token", sessionService.generateSession(authenticatedUser, session, sessionId).getRefreshToken(), "/", true));
        }


        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> getNewToken(@CookieValue(value = "session_id", defaultValue = "") String sessionId, @CookieValue(value = "refresh_token", defaultValue = "") String refreshToken) {
        log.info("Received refresh token : {}", refreshToken);

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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le code est invalide.");

        Session session = sessionService.generateSession(authenticatedUser, sessionRedisRepository.findByIdAndUserId(sessionId, authenticatedUser.getId()), sessionId);

        //      Step 3: Generate a jwt token
        final String token = jwtUtils.generateToken(authenticatedUser);
        TokenResponse responseBody = new TokenResponse();
        responseBody.setToken(token);

        //      Step 4: Generate session data and send it in a cookie
        response.addCookie(sessionService.buildCookie("refresh_token", session.getRefreshToken(), "/", true));
        response.addCookie(sessionService.buildCookie("session_id", session.getId(), "/", false));

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
        } else if (action.equals("block")) {
            deleteSession(id);
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action {" + action + "} is undefined");

        return session;
    }

    @DeleteMapping("/sessions/{id}/delete")
    public ResponseEntity deleteSession(@PathVariable String id) {
        sessionRedisRepository.delete(getSessionById(id));
        return ResponseEntity.ok("");
    }

}
