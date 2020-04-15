package com.akinobank.app.controllers.admin;


import com.akinobank.app.models.Admin;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.AdminRepository;
import com.akinobank.app.repositories.AgentRepository;
import com.akinobank.app.repositories.ClientRepository;
import com.akinobank.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@ResponseBody //The @ResponseBody annotation tells a controller that the object returned is automatically serialized into JSON , you will need it
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

        model.addAttribute("user", new User());
        return ADMIN_VIEWS_PATH + "forms/adduser";
    }
    @PostMapping("users/ajouter")
    public String addUser(@ModelAttribute User user) {
        user.setEmailConfirmed(false);
        userRepository.save(user);

        switch (user.getRole()) {
            case
                "ADMIN": adminRepository.save(Admin.builder().user(user).build()); break;
            default:
                agentRepository.save(Agent.builder().user(user).build()); break;
        }
        /// juste pour tester le form.
        return "redirect:/admin/users";
    }




}
