package com.akinobank.app.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Agence implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO) //generation auto
    private Long id;
    private Date dateDeCreation;
    private Date dateUpdate;

    private String ville , libelleAgence; // libelleAgenece : nom agence

    @ManyToOne
    @JoinColumn(name = "id_admin") // pour la relation : chaque agence a un seul admin
    private Admin admin;


}
