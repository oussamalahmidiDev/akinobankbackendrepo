package com.akinobank.app.services;


import com.akinobank.app.models.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service
@EnableScheduling
public class NotificationService {

    Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private AuthService authService;

    private List<Consumer<Notification>> listeners = new CopyOnWriteArrayList<>();

    // function to subscribe clients to server events (notifications)
    public void subscribe (Consumer<Notification> listener) {
        listeners.add(listener);
        logger.info("Added a listener, for a total of {} listener{}", listeners.size(), listeners.size() > 1 ? "s" : "");
    }

    // function to push notification
    public void publish (Notification notification) {
        logger.info("PROCESS NOTIF : {}", notification);

        // make sure the notification is sent only to the according client.
        if (notification.getClient().getId() == authService.getCurrentUser().getClient().getId())
            listeners.forEach(c -> c.accept(notification));

    }


}
