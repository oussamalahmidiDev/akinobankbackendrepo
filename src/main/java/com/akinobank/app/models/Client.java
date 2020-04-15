package com.akinobank.app.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity // pour la générer du table User
// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
@NoArgsConstructor
@AllArgsConstructor
public class Client implements Serializable {

    @Id // la cle prm
    @GeneratedValue(strategy = GenerationType.IDENTITY)//generation auto
    private Long id;

    private String photo ;

//    @NotBlank(message = "le numéro de téléphone est obligatoire")
    private String numeroTelephone;

    @CreationTimestamp
    private Date dateDeCreation;

    @UpdateTimestamp
    private Date dateUpdate;

    @ManyToOne
    @JoinColumn(name = "id_agent") // pour la relation : chaque client a un seul agent
//    @NotBlank(message = "l'agent est obligatoire")
    private Agent agent;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY) // pour la relation : chaque client a pls comptes
//    @NotBlank(message = "Au moins un compte")
    private Collection<Compte> comptes;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY) // pour la relation : chaque client a 0 ou pls notification
    private Collection<Notification> notifications;

    @OneToOne// chaque client a un seul compte user pour l'auth
    private User user;

    public Client(User user) {
        this.user=user;
    }

}
