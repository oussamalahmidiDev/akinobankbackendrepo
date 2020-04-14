package com.akinobank.app.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.swing.*;
import java.io.Serializable;
import java.util.Collection;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor

public class Admin implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO) //pour la generation auto
    private int id;


    private String nom , prenom , email ;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // pour ne pas afficher le mot de passe , just pour la modification
    private String motDePasse;

    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY) // pour la relation : chaque admin a pls agents
    Collection<Agent> agentCollection;

    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY)// pour la relation : chaque admin a pls agences
    Collection<Agence> agenceCollection;

    @OneToOne(mappedBy = "admin") // pour la relation : un admin a un compte user pour la auth
    private User user;


    public Admin(String nom,String prenom, String email, String password) {
        this.nom=nom;
        this.prenom=prenom;
        this.email=email;
        this.motDePasse=password;
    }
}
