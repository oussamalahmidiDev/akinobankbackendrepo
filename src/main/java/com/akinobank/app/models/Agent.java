package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Agent implements Serializable {

    @Id//la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO) // la generation auto
    private Long id;
    private String nom , prenom ;

    @Enumerated(EnumType.ORDINAL)
    @CreationTimestamp
    private Date dateDeCreation;

    @Enumerated(EnumType.ORDINAL)
    @UpdateTimestamp
    private Date dateUpdate;

    @OneToMany(targetEntity = Agent.class)
    protected List roles ;

    @ManyToOne
    @JoinColumn(name = "id_admin") // pour la relation : chaque agent a un seul admin
    private Admin admin;

    @ManyToOne
    @JoinColumn(name = "id_agence") // pour la relation : un agent affecter a une seule agence
    private Agence agence;

    @OneToMany(mappedBy = "agent", fetch = FetchType.LAZY)// pour la relation : chaque agent a pls clients
    private Collection<Client> clients;

    @OneToOne(mappedBy = "agent") // chaque agent a un seul compte user pour l'auth
    private User user;


}
