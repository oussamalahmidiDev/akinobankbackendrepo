package com.akinobank.app.models;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class User implements UserDetails { // We use interface UserDetials instead of create a User class and defend each method, because there s a lot of methods already exist and helpful for us


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "L'email est obligatoire")
    private String email ;
//    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6)
    private String password;
    private boolean emailConfirmed;
    @NotNull
    private String verificationToken;
    @NotNull
    private String nom , prenom  ;

    @NotBlank(message = "Le role est obligatoire")
    private String roles ;

    @OneToOne
    private Admin admin;

    @OneToOne
    private Agent agent;

    @OneToOne
    private Client client;



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
        list.add(new SimpleGrantedAuthority("ROLE_" + roles)); // ROLE_  : just a prefix , Spring build a prefix for every role its just a syntx ,for exemple if your role is USER then your authority(role) is ROLE_USER

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
