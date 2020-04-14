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
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Agent implements Serializable {

    @Id//la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO) // la generation auto
    private int id;
    private String nom , prenom , email;
    private Date dateDeCreation;
    private Date dateUpdate;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // pour ne pas afficher le mot de passe
    private String motDePasse ;

    @ManyToOne
    @JoinColumn(name = "id_admin") // pour la relation : chaque agent a un seul admin
    private Admin admin;

    @ManyToOne
    @JoinColumn(name = "id_agence") // pour la relation : un agent affecter a une seule agence
    private Agence agence;

    @OneToMany(mappedBy = "agent", fetch = FetchType.LAZY)// pour la relation : chaque agent a pls clients
    private Collection<Client> clientCollection;

    @OneToOne(mappedBy = "agent") // chaque agent a un seul compte user pour l'auth
    private User user;

    public Agent(String nom, String prenom, String email, String password, Admin admin, Agence agence) {
        this.nom=nom;
        this.prenom=prenom;
        this.email=email;
        this.motDePasse=password;
        this.admin=admin;
        this.agence=agence;
    }
}
