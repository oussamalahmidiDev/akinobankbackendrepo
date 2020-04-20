package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args@AllArgsConstructor
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Agent  {

    @Id//la cle prm
    @GeneratedValue(strategy = GenerationType.IDENTITY) // la generation auto
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_admin") // pour la relation : chaque agent a un seul admin
    @NotNull
//    @JsonIgnore
    @JsonIgnoreProperties({"agents", "agences"})
    private Admin admin;

    @ManyToOne
    @JoinColumn(name = "id_agence") // pour la relation : un agent affecter a une seule agence
    @NotNull
    @JsonIgnoreProperties({"admin", "agents"})
//    @JsonIgnore
    private Agence agence;

    @OneToMany(mappedBy = "agent", fetch = FetchType.LAZY)// pour la relation : chaque agent a pls clients
    @JsonIgnoreProperties({"agent","agence"})
//    @JsonIgnore
    private Collection<Client> clients;

    @OneToOne // pour la relation : un admin a un compte user pour la auth
    @JoinColumn(name = "id_user")
//    @JsonIgnore
    @JsonIgnoreProperties({"agent"})
    private User user;

    //Just for test

    public Agent(User user ,Admin admin , Agence agence) {
        this.user=user;
        this.admin=admin;
        this.agence=agence;
    }

}
