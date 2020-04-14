package com.akinobank.app.controllers.admin;


import com.akinobank.app.repositories.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
