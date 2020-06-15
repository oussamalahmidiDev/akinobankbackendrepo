package com.akinobank.app.controllers.client;

import com.akinobank.app.models.Notification;
import com.akinobank.app.repositories.NotificationRepository;
import com.akinobank.app.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/client/api/notifications")
public class ClientNotificationsController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuthService authService;

    @GetMapping
    List<Notification> getAll() {
        return notificationRepository
            .findAllByReceiver(
                authService.getCurrentUser(),
                PageRequest.of(0, 30, Sort.by("dateDeNotification").descending()));
    }

    @PostMapping("/mark_seen")
    public void markAllAsSeen() {
        notificationRepository.findAllByReceiver(authService.getCurrentUser())
            .forEach(notification -> {
                if (!notification.getLue()) {
                    notification.setLue(true);
                    notificationRepository.save(notification);
                }
            });
    }
}
