package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.id.UUIDGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.text.CollationElementIterator;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Compte implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO,generator = UUIDGenerator.UUID_GEN_STRATEGY_CLASS) //pour la generation auto mais pas avec des int ,avec des UUID : Universally Unique IDentifier
    private UUID numeroCompte;
    private double solde;
    private String intitule ;

    @Enumerated(EnumType.STRING)
    private String statut; // status : etat du compte : active-block-...etc

    @CreationTimestamp
    private Date dateDeCreation;

    @CreationTimestamp
    private Date dernierOperation;

    @UpdateTimestamp
    private Date dateUpdate;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)// pour ne pas afficher le code secret
    private String codeSecret; // le code ou le mot de pass pour accéder au compte

    @ManyToOne
    @JoinColumn(name = "id_client") // pour la relation : chaque compte a un seul client
    private Client client;

    @OneToMany(fetch = FetchType.LAZY)
    private Collection<Virement> virements; // pour la relation : chaque compte a 0 ou pls virement

    @OneToMany(fetch = FetchType.LAZY)
    private Collection<Recharge> recharges; //pour la relation : chaque compte a 0 ou pls recharge

}
