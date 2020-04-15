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
import javax.validation.constraints.Positive;
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
    private Long id;

    @NotBlank(message = "L'operateur est obligatoire")
    private String operateur ;

    @NotBlank(message = "le numéro de téléphone est obligatoire")
    private String numeroTelephone;

    @NotNull
    @Positive
    private double montant ;

    @CreationTimestamp
    private Date dateDeRecharge;



    @ManyToOne
    @JoinColumn(name = "uuid_compte") // pour la relation : chaque recharge appartient a un seul compte
    @NotNull
    private Compte compte;
}
