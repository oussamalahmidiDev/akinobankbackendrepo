package com.akinobank.app.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteCredentialsRequest {
    String numeroCompte;
    int codeSecret;
}
