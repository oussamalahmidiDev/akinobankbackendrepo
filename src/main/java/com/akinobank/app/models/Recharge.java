package com.akinobank.app.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Recharge implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO)//generation auto
    private int id;
    private String operateur , numeroTelephone;
    private double montant ;
    private Date dateRecharge ;


    @ManyToOne
    @JoinColumn(name = "uuid_compte") // pour la relation : chaque recharge appartient a un seul compte
    private Compte compte;
}
