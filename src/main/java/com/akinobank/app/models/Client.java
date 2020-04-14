package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Client implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO)//generation auto
    private int id;
    private String nom, prenom , photo , numeroTelephone;

    private String verificationToken ; //a discuter

    private Date dateDeCreation;
    private Date dateUpdate;

    @ManyToOne
    @JoinColumn(name = "id_agent") // pour la relation : chaque client a un seul agent
    private Agent agent;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY) // pour la relation : chaque client a pls comptes
    private Collection<Compte> compteCollection;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY) // pour la relation : chaque client a 0 ou pls notification
    private Collection<Notification> notificationCollection;

    @OneToOne(mappedBy = "client")// chaque client a un seul compte user pour l'auth
    private User user;

}
