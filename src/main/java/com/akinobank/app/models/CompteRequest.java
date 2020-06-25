package com.akinobank.app.models;

import lombok.Data;

@Data
public class CompteRequest {
    
    private String agentPassword;
    private String intitule;
    private double solde;

}
