package com.akinobank.app.models;

import com.akinobank.app.enumerations.CompteStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Random;


@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Compte implements Serializable {

    @Id
    @GeneratedValue(generator = "cn-generator")
    @GenericGenerator(name = "cn-generator", strategy = "com.akinobank.app.utilities.CreditCardNumberGenerator")
    // This CC number generated using Luhn Algorithm found in CreditCardNumberGenerator
    private String numeroCompte;

    @NotNull
    private double solde;

    @NotNull
    private String intitule ;

    @Enumerated(EnumType.STRING)
    private CompteStatus statut; // status : etat du compte : active-block-...etc

    @CreationTimestamp
    private Date dateDeCreation;

    @CreationTimestamp
    private Date dernierOperation;

    @UpdateTimestamp
    private Date dateUpdate;

    private int codeSecret;

    @ManyToOne
    @JoinColumn(name = "id_client") // pour la relation : chaque compte a un seul client
//    @NotBlank(message = "le client est obligatoire")
    private Client client;

    @OneToMany(mappedBy = "compte",fetch = FetchType.LAZY)
    private Collection<Virement> virements; // pour la relation : chaque compte a 0 ou pls virement

    @OneToMany(mappedBy = "compte",fetch = FetchType.LAZY)
    private Collection<Recharge> recharges; //pour la relation : chaque compte a 0 ou pls recharge


    // triggered at begining of transaction : generate default values for Compte
    @PrePersist
    void beforeInsert() {
        System.out.println("SETTING DEFAULT VALUES FOR COMPTE");
        solde = 0;
        statut = CompteStatus.ACTIVE;
        codeSecret = new Random().nextInt(90000000) + 10000000;
    }

    //just for test

    public Compte(String intitule, Client client) {

        this.intitule=intitule;
        this.client=client;

    }
}
