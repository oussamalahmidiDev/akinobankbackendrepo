package com.akinobank.app.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirementRequest {

    String numeroCompte;
    float montant;
    String notes;
    String numeroCompteDest;
    int codeSecret;

}
