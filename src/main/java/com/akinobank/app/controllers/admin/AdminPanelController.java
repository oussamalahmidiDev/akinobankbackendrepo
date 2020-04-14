package com.akinobank.app.controllers.admin;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPanelController {

    final String ADMIN_VIEWS_PATH = "views/admin/";

    @GetMapping("")
    public String index() {
        return ADMIN_VIEWS_PATH + "index";
    }

}
