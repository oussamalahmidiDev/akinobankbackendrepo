package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class TokenResponse {

    @JsonProperty("2fa_enabled")
    Boolean _2FaEnabled;

    String token;
    Date expireAt;
}
