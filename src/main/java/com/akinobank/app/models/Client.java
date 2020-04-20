package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Client implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.IDENTITY)//generation auto
    private Long id;

    private String photo ;

//    @NotNull


    @ManyToOne
    @JoinColumn(name = "id_agent") // pour la relation : chaque client a un seul agent
//    @NotNull
    @JsonIgnoreProperties({"clients", "agence", "admin",})
    private Agent agent;

    @ManyToOne
    @JoinColumn(name = "id_agence") // pour la relation : chaque client a un seul agent
//    @NotNull
    @JsonIgnoreProperties({"clients"})
    private Agence agence;

    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY) // pour la relation : chaque client a pls comptes
//    @NotNull
    @JsonIgnoreProperties({"client"})
    private Collection<Compte> comptes;

    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY)  // pour la relation : chaque client a 0 ou pls notification
    private Collection<Notification> notifications;

    @OneToOne // pour la relation : un admin a un compte user pour la auth
    @JoinColumn(name = "id_user")
    @JsonIgnoreProperties({"client", "authorities", "username"})
    private User user;

    //Just for test
    public Client(User user,Agent agent,Agence agence) {
        this.user=user;
        this.agent=agent;
        this.agence=agence;
    }

}
