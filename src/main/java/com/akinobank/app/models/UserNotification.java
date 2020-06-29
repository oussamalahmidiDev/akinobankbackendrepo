package com.akinobank.app.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.Serializable;

//@Data
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(NotificationListener.class)
public class UserNotification implements Serializable {

    @Id
    @JoinColumn
    @ManyToOne
//    @JsonIgnore
    @JsonUnwrapped
    private Notification notification;

    @Id
    @JoinColumn
    @ManyToOne
    @JsonIgnore
    private User receiver;

    private Boolean lue;

    @PrePersist
    public void intialValues() {
        lue = false;
    }
// colien
}

// Notification listener to send notification after persisting entity in the DB.
@Component
class NotificationListener {

    @Autowired
    private SimpMessagingTemplate template;

    @PostPersist
    void send (UserNotification notification) {
        template.convertAndSendToUser(notification.getReceiver().getEmail(), "topic/notifications", notification);

    }

}

