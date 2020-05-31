package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@SQLDelete(sql = "UPDATE demande SET deleted=true WHERE id=?")
@Where(clause = "deleted = false")
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nom;
    private String prenom;

    private Boolean acceptee;

    private boolean deleted;

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
