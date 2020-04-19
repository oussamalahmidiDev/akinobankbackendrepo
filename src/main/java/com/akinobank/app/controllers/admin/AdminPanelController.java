package com.akinobank.app.controllers.admin;


import com.akinobank.app.models.Admin;
import com.akinobank.app.models.Agence;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.MailService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
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

    @Autowired
    private MailService mailService;


    final String ADMIN_VIEWS_PATH = "views/admin/";


    @GetMapping("")
    public String index(Model model) {
        return ADMIN_VIEWS_PATH + "index";
    }

    @GetMapping("users")
    public String usersView(Model model) {
        model.addAttribute("users", userRepository.findAllByOrderByDateDeCreationDesc());
        return ADMIN_VIEWS_PATH + "users";
    }

    @GetMapping("users/ajouter")
    public String addUserView(Model model) {
        model.addAttribute("agences", agenceRepository.findAll());
        model.addAttribute("user", new User());
        return ADMIN_VIEWS_PATH + "forms/user.add";
    }
    @PostMapping("users/ajouter")
    public String addUser(@ModelAttribute User user, HttpServletRequest request) throws UnirestException {
//        if (user.getAgent() )
        if (user.getRole().name().equals("ADMIN")) {
//            System.out.println("SAVING ADMIN : " + user.toString());
            // pour eviter le probleme de transaction
            user.setAgent(null);
            user = userRepository.save(user);
            adminRepository.save(Admin.builder().user(user).build());
        }
        if (user.getRole().name().equals("AGENT")) {
//            System.out.println("SAVING AGENT : " + user.toString());
            Agence chosenAgence = user.getAgent().getAgence();
            // pour eviter le probleme de transaction
            user.setAgent(null);
            user = userRepository.save(user);
            // c juste parcequ'on n'a pas encore implemente l'auth.
            Admin admin = adminRepository.getOne((long) 1);
            agentRepository.save(Agent.builder().user(user).admin(admin).agence(chosenAgence).build());
        }
        String rootURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        // generation du lien de confirmation et envoie par mail
        String confirmationURL = rootURL + "/confirm?token=" + user.getVerificationToken();
//        mailService.sendVerificationMail(user, confirmationURL);
        mailService.sendVerificationMailViaMG(user);


        return "redirect:/admin/users";
    }

    @GetMapping("users/update/{id}")
    public String updateUserView(Model model, @PathVariable(value = "id") Long id) {
        model.addAttribute("user", userRepository.getOne(id));
        return ADMIN_VIEWS_PATH + "forms/user.update";
    }

    @PostMapping("users/update/{id}")
    public String updateUser(@ModelAttribute User user) {
//        System.out.println(user.toString());
        User userToUpdate = userRepository.getOne(user.getId());
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setNom(user.getNom());
        userToUpdate.setPrenom(user.getPrenom());

        userRepository.save(userToUpdate);
        return "redirect:/admin/users";
    }

    @PostMapping("users/delete/{id}")
    public String deleteUser(@PathVariable(value = "id") Long id) {
        User user = userRepository.getOne(id);
        agentRepository.delete(user.getAgent());
        userRepository.delete(user);

        return "redirect:/admin/users";
    }

    @GetMapping("agences")
    public String agencesView(Model model) {
//        model.addAttribute("agents", agentRepository.findAll());
        model.addAttribute("agences", agenceRepository.findAll());
        return ADMIN_VIEWS_PATH + "agences";
    }
    @GetMapping("agences/ajouter")
    public String addAgenceView(Model model) {
        model.addAttribute("agence", new Agence());
        return ADMIN_VIEWS_PATH + "forms/agence.add";
    }
    @PostMapping("agences/ajouter")
    public String addAgence(@ModelAttribute Agence agence) {
        Admin admin = adminRepository.getOne((long) 1);
        agenceRepository.save(Agence.builder().ville(agence.getVille()).libelleAgence(agence.getLibelleAgence()).admin(admin).build());
        return "redirect:/admin/agences";
    }
    @GetMapping("agences/update/{id}")
    public String updateAgenceView(Model model, @PathVariable(value = "id") Long id) {
        model.addAttribute("agence", agenceRepository.getOne(id));
        return ADMIN_VIEWS_PATH + "forms/agence.update";
    }
    @PostMapping("agences/update/{id}")
    public String updateAgence(@ModelAttribute Agence agence) {
//        System.out.println(agence.toString());
        Agence agenceToUpdate = agenceRepository.getOne(agence.getId());
        agenceToUpdate.setLibelleAgence(agence.getLibelleAgence());
        agenceToUpdate.setVille(agence.getVille());
        agenceRepository.save(agenceToUpdate);
        return "redirect:/admin/agences";
    }
    @PostMapping("agences/delete/{id}")
    public String deleteAgence(@PathVariable(value = "id") Long id) {
        Agence agence = agenceRepository.getOne(id);
        agenceRepository.delete(agence);
        return "redirect:/admin/agences";
    }

}
