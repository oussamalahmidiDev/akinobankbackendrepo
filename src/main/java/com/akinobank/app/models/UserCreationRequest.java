package com.akinobank.app.models;

import com.akinobank.app.enumerations.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreationRequest {
    String nom, prenom, email;
    Role role;
    Agence agence;
}
