package com.akinobank.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Le token est invalide")
public class InvalidVerificationTokenException extends RuntimeException {}
