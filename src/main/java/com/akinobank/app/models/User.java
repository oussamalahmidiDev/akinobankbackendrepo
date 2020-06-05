package com.akinobank.app.models;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.utilities.VerificationTokenGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

    //    @NotNull
    @Column(unique = true)
    @Email
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private boolean emailConfirmed;

    private String photo;
    private String adresse;
    private String ville;
    private Long codePostale;


    private Boolean archived;

    @JsonIgnore
    private String secretKey;

    private Boolean _2FaEnabled;

    @JsonIgnore
    private String refreshToken;


    @JsonIgnore
    private String verificationToken;

    private String numeroTelephone;

    //    @NotNull
    private String nom, prenom;

    //    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = {CascadeType.REMOVE})
//    @JsonIgnore
    private Admin admin;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
    @JsonIgnoreProperties({"user"})
    private Agent agent;

    @OneToOne(mappedBy = "user", cascade = {CascadeType.REMOVE})
//    @JsonIgnore
    @JsonIgnoreProperties({"user"})
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
        archived = false;
        _2FaEnabled = false;
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
}
