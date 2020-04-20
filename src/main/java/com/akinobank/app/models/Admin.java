package com.akinobank.app.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Admin implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pour la generation auto
    private Long id;

    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY) // pour la relation : chaque admin a pls agents
    Collection<Agent> agents;

    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY)// pour la relation : chaque admin a pls agences
    Collection<Agence> agences;

    @OneToOne // pour la relation : un admin a un compte user pour la auth
    @JoinColumn(name = "id_user")
    @JsonIgnoreProperties({"admin", "emailConfirmed", "dateUpdate", "dateDeCreation", })
    private User user;

    //Just for test
    public Admin(User user) {
        this.user=user;
    }

}
