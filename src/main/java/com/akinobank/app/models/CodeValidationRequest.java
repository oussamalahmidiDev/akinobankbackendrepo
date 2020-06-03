package com.akinobank.app.models;

import lombok.Data;

@Data
public class CodeValidationRequest {
    private Integer code;
    private String email;
    private String password;
}
