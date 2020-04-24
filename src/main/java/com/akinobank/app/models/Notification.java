package com.akinobank.app.models;

import com.akinobank.app.services.NotificationService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
@EntityListeners(NotificationListener.class) // Link Notification entity to NotifiationListener
public class Notification {


    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO) // generation auto
    private Long id;

    private String contenu ;
    private boolean lue;

//    @NotNull
    private String type;

    @CreationTimestamp
    private Date dateDeNotification;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false) // pour la relation : chaque Notification appartient a un seul client
    @JsonIgnore
    private Client client;

    @PrePersist
    void beforeInsert() {
        lue = false;
    }


}

// Notification listener to send notification after persisting entity in the DB.
@Component
class NotificationListener {

    @Autowired
    private NotificationService notificationService;

    @PostPersist
    void send (Notification notification) {
        notificationService.publish(notification);
    }

}
