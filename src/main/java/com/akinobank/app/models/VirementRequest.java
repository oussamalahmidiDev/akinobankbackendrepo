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
public class VirementRequest {

//    @NotNull(message = "Veuillez entrer le nº de compte")
    @CreditCardNumber
    String numeroCompte;
    float montant;

    String notes;

//    @NotNull(message = "Veuillez entrer le nº de compte de destination")
    @CreditCardNumber
    String numeroCompteDest;

//    @NotNull(message = "Veuillez entrer le code secret de compte")
    @Pattern(regexp="[\\d]{8}", message = "Le code secret doit contenir exactement 8 chiffres")
    String codeSecret;

}
