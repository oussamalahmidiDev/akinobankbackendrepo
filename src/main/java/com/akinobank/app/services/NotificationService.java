package com.akinobank.app.services;


import com.akinobank.app.models.Notification;
import com.akinobank.app.models.User;
import com.akinobank.app.models.UserNotification;
import com.akinobank.app.repositories.NotificationRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
//@EnableScheduling
@Log4j2
public class NotificationService {

    @Autowired
    private NotificationRepository repository;

    public void send(Notification notification, User...receivers) {

        List<UserNotification> userNotifications = new ArrayList<>();

        Arrays.stream(receivers).forEach(receiver -> {
            log.info("Adding receiver : {}", receiver.getEmail());
            UserNotification userNotification = UserNotification.builder()
                .notification(notification)
                .receiver(receiver)
                .build();
            userNotifications.add(userNotification);
        });

        log.info("Attaching receiver to the notif");
        notification.setUserNotification(userNotifications);

        log.info("Saving notif");
        repository.save(notification);
        log.info("Notif saved");
    }

}
