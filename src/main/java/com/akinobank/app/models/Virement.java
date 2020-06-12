package com.akinobank.app.models;

import com.akinobank.app.enumerations.VirementStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.util.Date;
import java.util.Random;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@SQLDelete(sql = "UPDATE virement SET deleted=true WHERE id_virement=?")
@Where(clause = "deleted = false")
public class Virement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_virement", unique = true)
    private Long id ;

    private String notes;

    private boolean deleted;

//    @NotNull
    @JsonIgnore
    private String codeVerification; // pour vérifier la transaction avant d'envoyer

//    @NotNull
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
        codeVerification = Integer.toString(new Random().nextInt(90000000) + 10000000);
//        codeVerification.toString();
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
