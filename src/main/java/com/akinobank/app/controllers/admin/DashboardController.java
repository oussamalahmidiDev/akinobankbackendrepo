package com.akinobank.app.controllers.admin;

import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private AgenceRepository agenceRepository;

    @Autowired
    private VilleRepository villeRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private ActivitiesService activitiesService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder encoder;

    Logger logger = LoggerFactory.getLogger(AdminPanelController.class);


    final String ADMIN_VIEWS_PATH = "views/admin/";

    @ModelAttribute("principal")
    public User addUserToModelAttribute() {
        return authService.getCurrentUser();
    }


    @GetMapping()
    public String index(Model model) {
        return ADMIN_VIEWS_PATH + "index";
    }


}
