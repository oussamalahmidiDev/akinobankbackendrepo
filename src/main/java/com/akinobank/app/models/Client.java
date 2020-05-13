package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonPropertyOrder({ "user" })
public class Client {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.IDENTITY )//generation auto
    private Long id;

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

    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY,  cascade={CascadeType.REMOVE}) // pour la relation : chaque client a pls comptes
//    @NotNull
    @JsonIgnoreProperties({"client"})
    private Collection<Compte> comptes;

    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY,  cascade={CascadeType.REMOVE})  // pour la relation : chaque client a 0 ou pls notification
    @JsonIgnore
    private Collection<Notification> notifications;

    @OneToOne(mappedBy = "client", fetch = FetchType.LAZY,  cascade={CascadeType.REMOVE})
    @JsonIgnoreProperties("client")
    private Demande demande;

    @OneToOne // pour la relation : un admin a un compte user pour la auth
    @JoinColumn(name = "id_user")
    @JsonIgnoreProperties({"id","client", "authorities", "username"})
    @JsonUnwrapped
    private User user;

    //Just for test
    public Client(User user,Agent agent,Agence agence) {
        this.user=user;
        this.agent=agent;
        this.agence=agence;
    }

}
