package com.akinobank.app.models;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
    private String confPassword;
}
