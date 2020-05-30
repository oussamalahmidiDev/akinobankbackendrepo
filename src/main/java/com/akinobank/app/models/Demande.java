package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nom;
    private String prenom;

    private Boolean acceptee;

    @ManyToOne // pour la relation : un admin a un compte user pour la auth
    @JoinColumn(name = "client_id")
//    @JsonIgnoreProperties({ "comptes", "notifications", "agent", "agence","photo" })
    @JsonIgnore
    private Client client;

    @UpdateTimestamp
    private Date timestamp;

    @PrePersist
    @PreUpdate
    public void beforeInsert () {
        acceptee = false;
    }



}
