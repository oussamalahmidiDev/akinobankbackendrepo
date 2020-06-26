package com.akinobank.app.services;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.models.Session;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.SessionRedisRepository;
import com.akinobank.app.utilities.SessionsUtils;
import com.akinobank.app.utilities.VerificationTokenGenerator;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.util.Date;

@Service
public class SessionService {

    @Autowired
    private SessionRedisRepository sessionRedisRepository;

    @Autowired
    private SessionsUtils sessionsUtils;

    @Autowired
    private ActivitiesService activitiesService;


    public Session generateSession(User user, Session oldSession, String sessionId) {
        CityResponse cityResponse = sessionsUtils.getCityResponse();
        Session newSession = Session.builder()
            .browser(sessionsUtils.getUserAgent().getBrowser().getName())
            .ip(sessionsUtils.getIpAdress())
            .ville(cityResponse != null? cityResponse.getCity().getName() : null)
            .pays(cityResponse != null? cityResponse.getCountry().getName() : null)
            .operatingSystem(sessionsUtils.getUserAgent().getOperatingSystem().getName())
            .userId(user.getId())
            .build()
            .init();
        if (sessionRedisRepository.findByIdAndUserId(sessionId, user.getId()) != null) {
            newSession.setId(sessionId);
            newSession.setAuthorized(oldSession.getAuthorized());
            newSession.setTimestamp(new Date());
            newSession.setRefreshToken(VerificationTokenGenerator.generateVerificationToken());

        }
        else {
            activitiesService.save(
                String.format("Détection d'une nouvelle connexion sur l'appareil %s, %s sur l'adresse IP : %s",
                    newSession.getOperatingSystem(), newSession.getBrowser(), newSession.getIp()),
                ActivityCategory.SESSIONS_C,
                user
            );
        }
        return sessionRedisRepository.save(newSession);
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
