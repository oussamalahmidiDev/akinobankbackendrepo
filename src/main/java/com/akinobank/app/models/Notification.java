package com.akinobank.app.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notification implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.AUTO) // generation auto
    private Long id;

    private String contenu ;
    private boolean lue;
    @NotNull
    private String type;

    @CreationTimestamp
    private Date dateDeNotification;


    @ManyToOne
    @JoinColumn(name = "id_client") // pour la relation : chaque Notification appartient a un seul client
    @NotNull
    private Client client;

}