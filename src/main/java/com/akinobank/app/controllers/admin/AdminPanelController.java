package com.akinobank.app.controllers.admin;


import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Admin;
import com.akinobank.app.models.Agence;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolationException;

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

    @RequestMapping("/login")
    public String login() {
//        Redirect user to hompage if he's already authenticated
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        logger.info("Auth : {}", auth.getPrincipal().toString());
//        if (!(auth instanceof AnonymousAuthenticationToken)) {
//            return "redirect:/admin";
//        }
        return ADMIN_VIEWS_PATH + "login";
    }

//    @PostMapping("/auth")
//    public String auth(HttpServletRequest request, Model model) {
//        logger.info("Requested : {}, {}", request.getParameter("email"), request.getParameter("password"));
//        model.addAttribute("email", request.getParameter("email"));
//        return login(model);
////        model.addAttribute()
//    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        return ADMIN_VIEWS_PATH + "login";
    }

    @GetMapping("")
    public String index(Model model) {
        return ADMIN_VIEWS_PATH + "index";
    }

    @GetMapping("users")
    public String usersView(Model model) {
        model.addAttribute("users", userRepository.findAllByRoleIsNot(Role.CLIENT));
        return ADMIN_VIEWS_PATH + "users";
    }

    @GetMapping("users/ajouter")
    public String addUserView(Model model) {
        model.addAttribute("agences", agenceRepository.findAll());
        if (!model.containsAttribute("user"))
            model.addAttribute("user", new User());
        return ADMIN_VIEWS_PATH + "forms/user.add";
    }

    @PostMapping("users/ajouter")
    public String addUser(@ModelAttribute User user, HttpServletRequest request, Model model) {
//        if (user.getAgent() )
        model.addAttribute("user", user);

        if (user.getNom().equals("") || user.getPrenom().equals("") || user.getEmail().equals("") || user.getRole() == null) {
            model.addAttribute("error", "Veuillez remplir tous les champs.");
            return addUserView(model);
        }

        User adminUser = authService.getCurrentUser();
        try {
            if (user.getRole().name().equals("ADMIN")) {
//            System.out.println("SAVING ADMIN : " + user.toString());
                // pour eviter le probleme de transaction
                user.setAgent(null);
                user = userRepository.save(user);
                adminRepository.save(Admin.builder().user(user).build());

                activitiesService.save(
                    String.format("Création d'un nouveau administrateur \"%s %s \"", user.getPrenom(), user.getNom()),
                    ActivityCategory.USERS_C
                );
            }
            if (user.getRole().name().equals("AGENT")) {
//            System.out.println("SAVING AGENT : " + user.toString());
                Agence chosenAgence = user.getAgent().getAgence();
                // pour eviter le probleme de transaction
                user.setAgent(null);
                user = userRepository.save(user);
                // c juste parcequ'on n'a pas encore implemente l'auth.
//                Admin admin = adminRepository.getOne((long) 1);
                agentRepository.save(Agent.builder().user(user).admin(adminUser.getAdmin()).agence(chosenAgence).build());

                activitiesService.save(
                    String.format("Création d'un nouveau agent \"%s %s \" dans l'agence %s (%s)", user.getPrenom(), user.getNom(), chosenAgence.getLibelleAgence(), chosenAgence.getVille().getNom()),
                    ActivityCategory.USERS_C
//                    adminUser
                );
            }
            mailService.sendVerificationMail(user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error_email", "Cet email existe déjà.");
            return addUserView(model);
        } catch (ConstraintViolationException e) {
            model.addAttribute("error", "Les données que vous avez entré sont invalides.");
            return addUserView(model);
        }


        return "redirect:/admin/users";
    }

    @GetMapping("users/update/{id}")
    public String updateUserView(Model model, @PathVariable("id") Long id) {
        if (!model.containsAttribute("user"))
            model.addAttribute("user", userRepository.findByIdAndRoleIsNot(id, Role.CLIENT).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable")
            ));
        return ADMIN_VIEWS_PATH + "forms/user.update";
    }

    @PostMapping("users/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, Model model) {
        try {
            model.addAttribute("user", user);

            User userToUpdate = userRepository.findByIdAndRoleIsNot(id, Role.CLIENT).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable")
            );
            if (!user.getEmail().equals(userToUpdate.getEmail())) {
                userToUpdate.setEmail(user.getEmail());
                mailService.sendVerificationMail(userToUpdate);
            }
            userToUpdate.setNom(user.getNom());
            userToUpdate.setPrenom(user.getPrenom());

            userRepository.save(userToUpdate);

            activitiesService.save(
                String.format("Modification des informations de l'utlisateur \"%s %s \"", user.getPrenom(), user.getNom()),
                ActivityCategory.USERS_U
            );

        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error_email", "Cet email appartient à un autre utilisateur.");
            return updateUserView(model, user.getId());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("users/delete/{id}")
    public String deleteUser(@PathVariable(value = "id") Long id, HttpServletRequest request) {
        String password = request.getParameter("password");
        logger.info("Received password : {}", password);
        if (!encoder.matches(password, authService.getCurrentUser().getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mot de passe incorrect");

        User user = userRepository.findByIdAndRoleIsNot(id, Role.CLIENT).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable")
        );
        // agentRepository.delete(user.getAgent());
        userRepository.delete(user);

        activitiesService.save(
            String.format("Suppression de l'utilisateur \"%s %s\"", user.getPrenom(), user.getNom()),
            ActivityCategory.USERS_D
        );

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
        if (!model.containsAttribute("agence"))
            model.addAttribute("agence", new Agence());

        model.addAttribute("villes", villeRepository.findAll());
        return ADMIN_VIEWS_PATH + "forms/agence.add";
    }

    @PostMapping("agences/ajouter")
    public String addAgence(@ModelAttribute Agence agence, Model model) {
        model.addAttribute("agence", agence);

        agence = Agence.builder().ville(agence.getVille()).libelleAgence(agence.getLibelleAgence()).admin(authService.getCurrentUser().getAdmin()).build();
        agenceRepository.save(agence);

        activitiesService.save(
            String.format("Création d'une nouvelle agence \"%s\" dans la ville %s", agence.getLibelleAgence(), agence.getVille().getNom()),
            ActivityCategory.AGENCES_C
//            admin.getUser()
        );
        return "redirect:/admin/agences";
    }

    @GetMapping("agences/update/{id}")
    public String updateAgenceView(Model model, @PathVariable(value = "id") Long id) {
        model.addAttribute("agence", agenceRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agence introuvable")
        ));
        model.addAttribute("villes", villeRepository.findAll());

        return ADMIN_VIEWS_PATH + "forms/agence.update";
    }

    @PostMapping("agences/update/{id}")
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

    @PostMapping("agences/delete/{id}")
    public String deleteAgence(@PathVariable(value = "id") Long id, HttpServletRequest request) {
        String password = request.getParameter("password");
        logger.info("Received password : {}", password);
        if (!encoder.matches(password, authService.getCurrentUser().getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mot de passe incorrect");


        Agence agence = agenceRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agence introuvable")
        );

        agenceRepository.delete(agence);

        activitiesService.save(
            String.format("Suppression de l'agence \"%s\"", agence.getLibelleAgence()),
            ActivityCategory.AGENCES_D
        );
        return "redirect:/admin/agences";
    }

}
