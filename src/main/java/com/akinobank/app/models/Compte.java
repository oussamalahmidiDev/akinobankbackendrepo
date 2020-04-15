package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.id.UUIDGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
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
//
//    @Id // la cle prm
//    @GeneratedValue(strategy = GenerationType.IDENTITY) //pour la generation auto mais pas avec des int ,avec des UUID : Universally Unique IDentifier
     @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
     @Column(name = "numero_compte", length = 16, unique = true, nullable = false)
    private UUID numeroCompte;
//    @NotNull
    private double solde;
//    @NotBlank(message = "intitule est obligatoire")
    private String intitule ;

//    @NotNull
    private String statut; // status : etat du compte : active-block-...etc

    @CreationTimestamp
    private Date dateDeCreation;

    @CreationTimestamp
    private Date dernierOperation;

    @UpdateTimestamp
    private Date dateUpdate;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)// pour ne pas afficher le code secret
//    @NotBlank(message = "Le code secret est obligatoire")
//    @Size(min = 4 , max = 8)
    private String codeSecret; // le code ou le mot de pass pour accéder au compte

    @ManyToOne
    @JoinColumn(name = "id_client") // pour la relation : chaque compte a un seul client
//    @NotBlank(message = "le client est obligatoire")
    private Client client;

    @OneToMany(fetch = FetchType.LAZY)
    // peut etre 0 mais pas null
    private Collection<Virement> virements; // pour la relation : chaque compte a 0 ou pls virement

    @OneToMany // pour la relation : chaque client a 0 ou pls notification
    private Collection<Notification> notifications;

    public void setNumeroCompte() {
        this.numeroCompte = UUID.randomUUID();;
    }

    @OneToMany(fetch = FetchType.LAZY)
    private Collection<Recharge> recharges; //pour la relation : chaque compte a 0 ou pls recharge

    //just for test

    public Compte(int solde, String intitule, String status, Date date, Date date1, Date date2, String codeS3, Client client) {

        this.solde=solde;
        this.intitule=intitule;
        this.statut=status;
        this.dateDeCreation=date;
        this.dateUpdate=date1;
        this.dernierOperation=date2;
        this.codeSecret=codeS3;
        this.client=client;

    }
}
