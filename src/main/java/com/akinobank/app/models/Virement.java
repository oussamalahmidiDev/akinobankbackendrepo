package com.akinobank.app.models;

import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.enumerations.VirementStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.istack.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.ValueGenerationType;
import org.hibernate.id.UUIDGenerationStrategy;
import org.hibernate.id.UUIDGenerator;

import javax.persistence.*;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Virement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_virement", unique = true)
    private Long id ;

    private String notes;

    @NotNull
    @JsonIgnore
    private int codeVerification; // pour vérifier la transaction avant d'envoyer

    @NotNull
    @Positive
    private double montant;

    @CreationTimestamp
    private Date dateDeVirement;

    @Enumerated(EnumType.STRING)
    private VirementStatus statut;

    // triggered at begining of transaction : generate default values for Virement
    @PrePersist
    void beforeInsert() {
        System.out.println("SETTING DEFAULT VALUES FOR VIREMENT");
        statut = VirementStatus.UNCOFIRMED;
        codeVerification = new Random().nextInt(90000000) + 10000000;
    }


    @ManyToOne
    @JoinColumn(name = "uuid_compte", nullable = false) // pour la relation : plusieur virement apprtient a un seul compte
    @JsonIgnoreProperties({"virements", "recharges", "client"})
    private Compte compte;

    @OneToOne
    @JoinColumn(name = "compte_destinataire")
    @JsonIgnoreProperties({"virements", "recharges", "client", "dateDeCreation", "dernierOperation", "intitule", "solde", "dateUpdate", "statut" })
    private Compte destCompte;  //la relation entre Virement 1,1 ---->1,1 Compte , envoyer 1 virement a un seul compte
    //a discuter


}
