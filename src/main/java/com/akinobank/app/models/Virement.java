package com.akinobank.app.models;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
import java.util.UUID;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Virement implements Serializable {

    @Id
    @GeneratedValue(generator = "hibernate-uuid")
    @GenericGenerator(name = "hibernate-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "numeroCompte", unique = true)
    private String id ;

    private String notes;

    @NotNull
    @Size(min = 4,max=8)
    private String codeVerification; // pour vérifier la transaction avant d'envoyer

    @NotNull
    @Positive
    private double montant;

    @CreationTimestamp
    private Date dateDeVirement;


    @ManyToOne
    @JoinColumn(name = "uuid_compte") // pour la relation : plusieur virement apprtient a un seul compte
    @NotNull
    private Compte compte;

    @OneToOne
    @JoinColumn(name = "compte_destinataire")
    private Compte destCompte;  //la relation entre Virement 1,1 ---->1,1 Compte , envoyer 1 virement a un seul compte
    //a discuter


}
