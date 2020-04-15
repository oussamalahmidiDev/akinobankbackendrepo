package com.akinobank.app.models;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User implements UserDetails { // We use interface UserDetials instead of create a User class and defend each method, because there s a lot of methods already exist and helpful for us


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email , password;
    private boolean emailConfirmed;
    private String verificationToken;

//    @Basic
//    private List roles = new ArrayList();
//
//    @OneToMany(targetEntity = Admin.class)
//    private List roles = new ArrayList<>();


    @OneToOne
    private Admin admin;

    @OneToOne
    private Agent agent;

    @OneToOne
    private Client client;



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
        list.add(new SimpleGrantedAuthority("ROLE_" + admin.getRoles())); // ROLE_  : just a prefix , Spring build a prefix for every role its just a syntx ,for exemple if your role is USER then your authority(role) is ROLE_USER
        list.add(new SimpleGrantedAuthority("ROLE_" + agent.getRoles()));
        list.add(new SimpleGrantedAuthority("ROLE_" + client.getRoles()));

        return list;
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
