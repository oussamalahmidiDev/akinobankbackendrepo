package com.akinobank.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Les mots de passes ne sont pas identiques.")
public class ConfirmationPasswordException extends RuntimeException {}
