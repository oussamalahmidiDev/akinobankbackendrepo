package com.akinobank.app.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

// controlleur personnalisé pour les vues d'erreurs. à utiliser dans la phase de prod
//@Controller
public class CustomErrorController implements ErrorController {

    final String ERROR_VIEWS_PATH = "views/errors/";

    Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        Integer statusCode = Integer.valueOf(status.toString());

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return ERROR_VIEWS_PATH + "404";
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return ERROR_VIEWS_PATH + "500";
        } else if (statusCode == HttpStatus.CONFLICT.value()) {
            return ERROR_VIEWS_PATH + "409";
        }
        model.addAttribute("code", statusCode);
        return ERROR_VIEWS_PATH + "any";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
