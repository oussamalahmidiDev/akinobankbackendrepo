package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args@AllArgsConstructor
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
public class Agent implements Serializable {

    @Id//la cle prm
    @GeneratedValue(strategy = GenerationType.IDENTITY) // la generation auto
    private Long id;

    @CreationTimestamp
    private Date dateDeCreation;

    @UpdateTimestamp
    private Date dateUpdate;

    @ManyToOne
    @JoinColumn(name = "id_admin") // pour la relation : chaque agent a un seul admin
//    @NotBlank(message = "Admin is obligatory")
    private Admin admin;

    @ManyToOne
    @JoinColumn(name = "id_agence") // pour la relation : un agent affecter a une seule agence
//    @NotBlank(message = "Agence is obligatory")
    private Agence agence;

    @OneToMany(mappedBy = "agent", fetch = FetchType.LAZY)// pour la relation : chaque agent a pls clients
    private Collection<Client> clients;

    @OneToOne // pour la relation : un admin a un compte user pour la auth
    @JoinColumn(name = "id_user")
    private User user;

    public Agent(User user ,Admin admin , Agence agence) {
        this.user=user;
        this.admin=admin;
        this.agence=agence;
    }

}
