package com.akinobank.app.models;

import com.akinobank.app.models.logs.ClientLogs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Client implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.IDENTITY)//generation auto
    private Long id;

    private String photo ;

    @NotNull
    private String numeroTelephone;


    @ManyToOne
    @JoinColumn(name = "id_agent") // pour la relation : chaque client a un seul agent
    @NotNull
    private Agent agent;

    @ManyToOne
    @JoinColumn(name = "id_agence") // pour la relation : chaque client a un seul agent
    @NotNull
    private Agence agence;

    @OneToMany // pour la relation : chaque client a pls comptes
    @NotNull
    private Collection<Compte> comptes;

    @OneToMany // pour la relation : chaque client a 0 ou pls notification
    private Collection<Notification> notifications;

    @OneToOne // pour la relation : un admin a un compte user pour la auth
    @JoinColumn(name = "id_user")
    private User user;

    @OneToOne
    private ClientLogs clientLogs;

    public Client(User user,Agent agent,Agence agence) {
        this.user=user;
        this.agent=agent;
        this.agence=agence;
    }

}
