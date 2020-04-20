package com.akinobank.app.models;
import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.enumerations.Role;
import com.akinobank.app.utilities.VerificationTokenGenerator;
import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.transaction.Transactional;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class User implements UserDetails { // We use interface UserDetials instead of create a User class and defend each method, because there s a lot of methods already exist and helpful for us


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(unique = true)
    @Email
    private String email ;

    private String password;

    private boolean emailConfirmed ;


    @JsonIgnore
    private String verificationToken;

    private String numeroTelephone;

//    @NotNull
    private String nom , prenom  ;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role ;

    @OneToOne(mappedBy = "user")
//    @JsonIgnore
    private Admin admin;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"user"})
    private Agent agent;

    @OneToOne(mappedBy = "user")
//    @JsonIgnore
    private Client client;

    @CreationTimestamp
    private Date dateDeCreation;

    @UpdateTimestamp
    private Date dateUpdate;

    // triggered at begining of transaction : generate default values for User
    @PrePersist
    void beforeInsert() {
        System.out.println("SETTING DEFAULT VALUES FOR USER");
        emailConfirmed = false;
        verificationToken = VerificationTokenGenerator.generateVerificationToken();
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
        list.add(new SimpleGrantedAuthority("ROLE_" + role)); // ROLE_  : just a prefix , Spring build a prefix for every role its just a syntx ,for exemple if your role is USER then your authority(role) is ROLE_USER

        return list;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    //Just for test
    public User(String nom, String prenom, String email, Role role ) {
        this.nom=nom;
        this.prenom=prenom;
        this.email=email;
        this.role=role;

    }

}
