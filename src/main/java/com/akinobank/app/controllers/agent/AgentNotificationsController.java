package com.akinobank.app.controllers.agent;

import com.akinobank.app.models.UserNotification;
import com.akinobank.app.repositories.NotificationRepository;
import com.akinobank.app.repositories.UserRepository;
import com.akinobank.app.services.AuthService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agent/api/notifications")
@Log4j2
public class AgentNotificationsController {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;


    @GetMapping()
    public List<UserNotification> getAllNotifications() {
        log.info("GET ALL NOTIFICATIONS ");
        log.info(authService.getCurrentUser().getUserNotification());

        return authService.getCurrentUser().getUserNotification().stream()
                .sorted((o1, o2) -> (int) (o2.getNotification().getTimestamp().getTime() - o1.getNotification().getTimestamp().getTime()))
                .collect(Collectors.toList());
//        return authService.getCurrentUser().getUserNotification()
    }

    @PostMapping("/mark_seen")
    public void markAllAsSeen() {
        List<UserNotification> notifications = authService.getCurrentUser().getUserNotification()
                .stream().map(userNotification -> {
                    userNotification.setLue(true);
                    return userNotification;
                }).collect(Collectors.toList());
        authService.getCurrentUser().setUserNotification(notifications);
        userRepository.save(authService.getCurrentUser());
    }
}
