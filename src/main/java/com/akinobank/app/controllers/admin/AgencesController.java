package com.akinobank.app.controllers.admin;


import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.models.Agence;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/agences")
public class AgencesController {

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
    public String agencesView(Model model) {
//        model.addAttribute("agents", agentRepository.findAll());
        model.addAttribute("agences", agenceRepository.findAll());
        return ADMIN_VIEWS_PATH + "agences";
    }

    @GetMapping("ajouter")
    public String addAgenceView(Model model) {
        if (!model.containsAttribute("agence"))
            model.addAttribute("agence", new Agence());

        model.addAttribute("villes", villeRepository.findAll());
        return ADMIN_VIEWS_PATH + "forms/agence.add";
    }

    @PostMapping("ajouter")
    public String addAgence(@ModelAttribute Agence agence, Model model) {
        model.addAttribute("agence", agence);

        agence = Agence.builder().ville(agence.getVille()).libelleAgence(agence.getLibelleAgence()).build();
        agenceRepository.save(agence);

        activitiesService.save(
            String.format("Création d'une nouvelle agence \"%s\" dans la ville %s", agence.getLibelleAgence(), agence.getVille().getNom()),
            ActivityCategory.AGENCES_C
//            admin.getUser()
        );
        return "redirect:/admin/agences";
    }

    @GetMapping("update/{id}")
    public String updateAgenceView(Model model, @PathVariable(value = "id") Long id) {
        model.addAttribute("agence", agenceRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agence introuvable")
        ));
        model.addAttribute("villes", villeRepository.findAll());

        return ADMIN_VIEWS_PATH + "forms/agence.update";
    }

    @PostMapping("update/{id}")
    public String updateAgence(@PathVariable Long id, @ModelAttribute Agence agence) {
//        System.out.println(agence.toString());
        Agence agenceToUpdate = agenceRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agence introuvable")
        );
        agenceToUpdate.setLibelleAgence(agence.getLibelleAgence());
        agenceToUpdate.setVille(agence.getVille());
        agenceRepository.save(agenceToUpdate);

        activitiesService.save(
            String.format("Modification des informations de l'agence \"%s\"", agence.getLibelleAgence()),
            ActivityCategory.AGENCES_C
        );
        return "redirect:/admin/agences";
    }

    @PostMapping("delete/{id}")
    public String deleteAgence(@PathVariable(value = "id") Long id, HttpServletRequest request) {
        String password = request.getParameter("password");
        logger.info("Received password : {}", password);
        if (!encoder.matches(password, authService.getCurrentUser().getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mot de passe incorrect.");


        Agence agence = agenceRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agence introuvable.")
        );

        agenceRepository.delete(agence);

        activitiesService.save(
            String.format("Suppression de l'agence \"%s\"", agence.getLibelleAgence()),
            ActivityCategory.AGENCES_D
        );
        return "redirect:/admin/agences";
    }

}
