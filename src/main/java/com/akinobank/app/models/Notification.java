package com.akinobank.app.models;

import com.akinobank.app.enumerations.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@Data
@Builder
@ToString
@EntityListeners(NotificationListener.class) // Link Notification entity to NotifiationListener
public class Notification {


    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO) // generation auto
    private Long id;

    private String contenu ;

    private Boolean lue;

//    @NotNull
    private NotificationType type;

    @CreationTimestamp
    private Date dateDeNotification;

//    @ManyToOne
//    @JoinColumn(name = "id_client", nullable = false) // pour la relation : chaque Notification appartient a un seul client
//    @JsonIgnore
//    private Client client;

    @ManyToMany
    private List<User> receiver;

    public Notification() {
        type = NotificationType.INFO;
    }

    @PrePersist
    void beforeInsert() {
        lue = false;
    }


}

// Notification listener to send notification after persisting entity in the DB.
@Component
class NotificationListener {

    @Autowired
    private SimpMessagingTemplate template;

    @PostPersist
    void send (Notification notification) {
        notification.getReceiver().forEach(receiver -> template.convertAndSendToUser(receiver.getEmail(), "/topic/notifications", notification));
    }

}
