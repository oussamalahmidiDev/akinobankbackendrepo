package com.akinobank.app.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechargeRequest {

    String numeroCompte;
    float montant;
    String operateur;
    String numeroTelephone;
    int codeSecret;

}
