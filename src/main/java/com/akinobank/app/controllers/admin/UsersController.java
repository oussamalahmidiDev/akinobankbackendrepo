package com.akinobank.app.controllers.admin;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.*;
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
import javax.validation.ConstraintViolationException;

@Controller
@RequestMapping("/admin/users")
public class UsersController {

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
    public String usersView(Model model) {
        model.addAttribute("users", userRepository.findAllByRoleIsNot(Role.CLIENT));
        return ADMIN_VIEWS_PATH + "users";
    }


    @GetMapping("ajouter")
    public String addUserView(Model model) {
        model.addAttribute("agences", agenceRepository.findAll());
        if (!model.containsAttribute("user"))
            model.addAttribute("user", new UserCreationRequest());
        return ADMIN_VIEWS_PATH + "forms/user.add";
    }

    @PostMapping("ajouter")
    public String addUser(@ModelAttribute UserCreationRequest body, HttpServletRequest request, Model model) {
//        if (user.getAgent() )
        model.addAttribute("user", body);


        if (body.getNom().equals("") || body.getPrenom().equals("") || body.getEmail().equals("") || body.getRole() == null) {
            model.addAttribute("error", "Veuillez remplir tous les champs.");
            return addUserView(model);
        }

        User user = User.builder()
            .email(body.getEmail())
            .nom(body.getNom())
            .prenom(body.getPrenom())
            .role(body.getRole())
            .build();

        User adminUser = authService.getCurrentUser();
        try {
            if (body.getRole().equals(Role.ADMIN)) {
//            System.out.println("SAVING ADMIN : " + user.toString());
                // pour eviter le probleme de transaction
                activitiesService.save("Authentification d'un admin", ActivityCategory.AUTH);
                userRepository.save(user);
                adminRepository.save(Admin.builder().user(user).build());

                activitiesService.save(
                    String.format("Création d'un nouveau administrateur \"%s %s \"", user.getPrenom(), user.getNom()),
                    ActivityCategory.USERS_C
                );
            }
            if (body.getRole().equals(Role.AGENT)){
//            System.out.println("SAVING AGENT : " + user.toString());
                Agence chosenAgence = body.getAgence();
                // pour eviter le probleme de transaction
                userRepository.save(user);
                // c juste parcequ'on n'a pas encore implemente l'auth.
//                Admin admin = adminRepository.getOne((long) 1);
                agentRepository.save(Agent.builder().user(user).agence(chosenAgence).build());

                activitiesService.save(
                    String.format("Création d'un nouveau agent \"%s %s \" dans l'agence %s (%s)", user.getPrenom(), user.getNom(), chosenAgence.getLibelleAgence(), chosenAgence.getVille().getNom()),
                    ActivityCategory.USERS_C
//                    adminUser
                );
            }
            mailService.sendVerificationMail(user);
            activitiesService.save("Confirmation d'email",ActivityCategory.EMAIL_CONF);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error_email", "Cet email existe déjà.");
            return addUserView(model);
        } catch (ConstraintViolationException e) {
            model.addAttribute("error", "Les données que vous avez entré sont invalides.");
            return addUserView(model);
        }


        return "redirect:/admin/users";
    }


    @GetMapping("update/{id}")
    public String updateUserView(Model model, @PathVariable("id") Long id) {
        if (!model.containsAttribute("user"))
            model.addAttribute("user", userRepository.findByIdAndRoleIsNot(id, Role.CLIENT).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable")
            ));
        return ADMIN_VIEWS_PATH + "forms/user.update";
    }

    @PostMapping("update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, Model model) {
        try {
            model.addAttribute("user", user);

            User userToUpdate = userRepository.findByIdAndRoleIsNot(id, Role.CLIENT).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable")
            );
            if (!user.getEmail().equals(userToUpdate.getEmail())) {
                userToUpdate.setEmail(user.getEmail());
                mailService.sendVerificationMail(userToUpdate);
                activitiesService.save("Confirmation d'email", ActivityCategory.EMAIL_CONF);
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

    @PostMapping("delete/{id}")
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

}
