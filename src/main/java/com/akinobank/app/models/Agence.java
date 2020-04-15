package com.akinobank.app.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
    @JoinColumn(name = "id_admin") // pour la relation : chaque agence a un seul admin
    private Admin admin;


    public Agence(String ville, String libelle, Admin admin) {
        this.ville=ville;
        this.libelleAgence=libelle;
        this.admin=admin;
    }
}
