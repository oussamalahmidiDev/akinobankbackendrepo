//package com.akinobank.app.models;
//
// << We create this class just in case UserDetails is not working >>
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import javax.persistence.*;
//import java.io.Serializable;
//
//@Entity // pour la générer du table User
//// annotation de Lombok : pour générer les getters&setters et les constructeurs par default et avec des args
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//
////ps use userdetails
//public class User implements Serializable {
//
//    @Id // la cle prm
//    @GeneratedValue(strategy = GenerationType.AUTO) // generation aut
//    private int id ;
//    private String email , roles ; // roles : possible 1 ou pls
//
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // pour ne pas afficher le mot de passe
//    private String motDePasse ;
//
//    @OneToOne
//    @JoinColumn(name = "id_admin") // pour la relation : chaque user a un seul admin
//    private Admin admin;
//
//    @OneToOne
//    @JoinColumn(name = "id_agent")// pour la relation : chaque user a un seul agent
//    private Agent agent;
//
//    @OneToOne
//    @JoinColumn(name = "id_client")// pour la relation : chaque user a un seul client
//    private Client client;
//}
