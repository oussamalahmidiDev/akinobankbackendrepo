package com.akinobank.app.models;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails { // We use interface UserDetials instead of create a User class and defend each method, because there s a lot of methods already exist and helpful for us


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email , password;

    @OneToOne
    private Admin admin;

    @OneToOne
    private Agent agent;

    @OneToOne
    private Client client;

//    @OneToMany
//    @JoinColumn("id")
//    private Collection role;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
