package com.akinobank.app.services;

import com.akinobank.app.models.Session;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.SessionRepository;
import com.akinobank.app.utilities.SessionsUtils;
import com.akinobank.app.utilities.VerificationTokenGenerator;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.Optional;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionsUtils sessionsUtils;


    public Session generateSession(User user, Optional<Session> oldSession, String sessionId) {
        CityResponse cityResponse = sessionsUtils.getCityResponse();
        Session newSession = Session.builder()
            .browser(sessionsUtils.getUserAgent().getBrowser().getName())
            .ip(sessionsUtils.getIpAdress())
            .ville(cityResponse != null? cityResponse.getCity().getName() : null)
            .pays(cityResponse != null? cityResponse.getCountry().getName() : null)
            .operatingSystem(sessionsUtils.getUserAgent().getOperatingSystem().getName())
            .user(user)
            .build();
        if (sessionRepository.existsByIdAndUser(sessionId, user)) {
            newSession.setId(sessionId);
            newSession.setAuthorized(oldSession.get().getAuthorized());
            newSession.setTimestamp(new Date());
            newSession.setRefreshToken(VerificationTokenGenerator.generateVerificationToken());
        }
        return sessionRepository.save(newSession);
    }

    public Cookie buildCookie(String name, String value, String path, Boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setHttpOnly(httpOnly);
        return cookie;
    }

    public Cookie buildCookie(String name, String value, String path, Boolean httpOnly, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge);
        return cookie;
    }

}
