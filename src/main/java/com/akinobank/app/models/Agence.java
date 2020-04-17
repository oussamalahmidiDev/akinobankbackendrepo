package com.akinobank.app.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Agence implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.IDENTITY) //generation auto
    private Long id;

    @CreationTimestamp
    private Date dateDeCreation;

    @UpdateTimestamp
    private Date dateUpdate;

    @NotNull
    private String ville , libelleAgence; // libelleAgenece : nom agence

    @ManyToOne
    @JoinColumn(name = "id_admin", nullable = false) // pour la relation : chaque agence a un seul admin
    private Admin admin;

    @OneToMany(mappedBy = "agence", fetch = FetchType.LAZY)// pour la relation : chaque agence a pls agents
    private Collection<Client> agents;


    //Just for test
    public Agence(String ville, String libelle, Admin admin) {
        this.ville=ville;
        this.libelleAgence=libelle;
        this.admin=admin;
    }
}
