package com.akinobank.app.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Date;


@Data
public class ResponseException extends RuntimeException {

    private HttpStatus status;
    private String[] errors;
    private Date timestamp;

    public ResponseException (HttpStatus status) {
        timestamp = new Date();
        this.status = status;
    }

    public ResponseException (HttpStatus status, String... errors) {
        timestamp = new Date();
        this.status = status;
        this.errors = errors;
    }

}
