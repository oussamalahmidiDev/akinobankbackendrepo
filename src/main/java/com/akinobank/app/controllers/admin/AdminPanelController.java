package com.akinobank.app.controllers.admin;


import com.akinobank.app.models.Admin;
import com.akinobank.app.models.Agence;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Controller
//@ResponseBody //The @ResponseBody annotation tells a controller that the object returned is automatically serialized into JSON , you will need it
@RequestMapping("/admin")
public class AdminPanelController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AgenceRepository agenceRepository;


    final String ADMIN_VIEWS_PATH = "views/admin/";


    @GetMapping("")
    public String index(Model model) {
        return ADMIN_VIEWS_PATH + "index";
    }

    @GetMapping("users")
    public String usersView(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return ADMIN_VIEWS_PATH + "users";
    }

    @GetMapping("users/ajouter")
    public String addUserView(Model model) {
        /// juste pour tester le form.
        User test = User.builder().email("test").password("hello").build();
        model.addAttribute("agences", agenceRepository.findAll());
        model.addAttribute("user", new User());
        return ADMIN_VIEWS_PATH + "forms/adduser";
    }
    @PostMapping("users/ajouter")
    public String addUser(@ModelAttribute User user) {
//        System.out.println(user.toString());
        Agence chosenAgence = user.getAgent().getAgence();
        if (user.getAgent().getId() == null) {
            System.out.println(user.toString());
            user.setAgent(null);
        }
        user.setVerificationToken(UUID.randomUUID().toString());
        user = userRepository.save(user);

        if (user.getRole().equals("ADMIN")) {
            adminRepository.save(Admin.builder().user(user).build());
        } else {
            Admin admin = adminRepository.findById((long) 1).get();
            agentRepository.save(Agent.builder().user(user).admin(admin).agence(chosenAgence).build());
        }
        return "redirect:/admin/users";
    }




}
