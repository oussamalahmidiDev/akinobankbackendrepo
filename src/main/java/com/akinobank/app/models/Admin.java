package com.akinobank.app.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.*;

import javax.persistence.*;
import java.util.Collection;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonPropertyOrder({ "user" })
public class Admin {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pour la generation auto
    private Long id;

   // @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY,  cascade={CascadeType.REMOVE}) // pour la relation : chaque admin a pls agents
    //Collection<Agent> agents;

    //@OneToMany(mappedBy = "admin", fetch = FetchType.LAZY,  cascade={CascadeType.REMOVE})// pour la relation : chaque admin a pls agences
    //Collection<Agence> agences;

    @OneToOne // pour la relation : un admin a un compte user pour la auth
    @JoinColumn(name = "id_user")
    @JsonIgnoreProperties({"admin", "emailConfirmed", "dateUpdate", "dateDeCreation", "id" })
    @JsonUnwrapped
    private User user;

    //Just for test
    public Admin(User user) {
        this.user=user;
    }

}
