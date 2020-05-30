package com.akinobank.app.models;


import com.akinobank.app.enumerations.RechargeStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
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
@Builder
public class Recharge implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO)//generation auto
    private Long id;

//    @NotNull
    private String operateur ;

//    @NotNull
    private String numeroTelephone;

//    @NotNull
//    @Positive
    private double montant ;
    private RechargeStatus statut;

    @CreationTimestamp
    private Date dateDeRecharge;

    @ManyToOne
    @JoinColumn(name = "uuid_compte", nullable = false) // pour la relation : chaque recharge appartient a un seul compte
    @JsonIgnoreProperties({"recharges", "virements", "client"})
    private Compte compte;

}
