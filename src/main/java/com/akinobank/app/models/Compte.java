package com.akinobank.app.models;

import com.akinobank.app.enumerations.CompteStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.*;
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
@SQLDelete(sql = "UPDATE compte SET deleted=true WHERE id=?")
@Where(clause = "deleted = false")
@JsonPropertyOrder({ "numeroCompte" })

public class Compte  {

    @Id
    @GeneratedValue(generator = "cn-generator")
    @GenericGenerator(name = "cn-generator", strategy = "com.akinobank.app.utilities.CreditCardNumberGenerator")
    // This CC number generated using Luhn Algorithm found in CreditCardNumberGenerator
    // @JsonIgnore
    @CreditCardNumber
    private String numeroCompte;

//    @Positive
    private double solde;

//    @NotNull
    private String intitule ;

    @Enumerated(EnumType.STRING)
    private CompteStatus statut; // status : etat du compte : active-block-...etc

    @CreationTimestamp
    private Date dateDeCreation;

    private Date dernierOperation;

    @UpdateTimestamp
    private Date dateUpdate;

//    @Size(min = 8 ,max = 8)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String codeSecret;

    @ManyToOne
    @JoinColumn(name = "id_client") // pour la relation : chaque compte a un seul client
//    @NotBlank(message = "le client est obligatoire")
    @JsonIgnoreProperties({"comptes", "notifications"})
    @JsonIgnore
    private Client client;

    private boolean deleted;

    private String raison;


    @OneToMany(mappedBy = "compte",fetch = FetchType.LAZY,  cascade={CascadeType.REMOVE})
    @JsonIgnoreProperties({"compte"})
    private Collection<Virement> virements; // pour la relation : chaque compte a 0 ou pls virement

    @OneToMany(mappedBy = "compte",fetch = FetchType.LAZY,  cascade={CascadeType.REMOVE})
    private Collection<Recharge> recharges; //pour la relation : chaque compte a 0 ou pls recharge


    // triggered at begining of transaction : generate default values for Compte
    @PrePersist
    void beforeInsert() {
        System.out.println("SETTING DEFAULT VALUES FOR COMPTE");
        //solde = 0;
        statut = CompteStatus.ACTIVE;
        codeSecret = String.valueOf(new Random().nextInt(90000000) + 10000000);
    }

    // @JsonProperty("numeroCompte")
    public String getNumeroCompteHidden() {
        // we will add condition on roles on this getter, for the moment let's hide the field on everyone.
        return new String(new char[8]).replace('\0', '*').concat(numeroCompte.substring(12));
    }

    public Compte(String intitule, Client client,double solde) {

        this.intitule=intitule;
        this.client=client;
        this.solde = solde;

    }
}
