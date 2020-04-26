package com.akinobank.app.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechargeRequest {

    @NotNull(message = "Veuillez entrer le nº de compte")
    @CreditCardNumber
    String numeroCompte;

    float montant;
    String operateur;

    @Pattern(regexp = "^\\+(?:[0-9] ?){6,14}[0-9]$", message = "Le nº de télephone est invalide.")
    String numeroTelephone;

    @NotNull(message = "Veuillez entrer le code secret de compte")
    @Pattern(regexp="[\\d]{8}", message = "Le code secret doit contenir exactement 8 chiffres")
    String codeSecret;

}
