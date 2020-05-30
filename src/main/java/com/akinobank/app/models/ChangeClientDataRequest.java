package com.akinobank.app.models;

import lombok.Data;

@Data
public class ChangeClientDataRequest {
    String agentPassword;
    User user;
}
