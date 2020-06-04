package com.akinobank.app.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeChangeRequest {

//    @NotNull(message = "Veuillez entrer le nº de compte")
    @CreditCardNumber
    String numeroCompte;

//    @NotNull(message = "Veuillez entrer le code secret de compte")
    String codeSecret;

//    @NotNull
//    @Digits(integer=8, fraction=0, message = "8 chiffres")
    @Pattern(regexp="[\\d]{8}", message = "Le code doit contenir exactement 8 chiffres")
    String newCodeSecret;

//    @NotNull
    String newCodeSecretConf;

}
