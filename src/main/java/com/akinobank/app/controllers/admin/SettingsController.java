package com.akinobank.app.controllers.admin;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/settings")
public class SettingsController {

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
    public String settings(Model model) {

        model.addAttribute("user", authService.getCurrentUser());
        return ADMIN_VIEWS_PATH + "settings";
    }

    @PostMapping("profil")
    public String updateProfile(@ModelAttribute User user, HttpServletRequest request, Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            currentUser.setPrenom(user.getPrenom());
            currentUser.setNom(user.getNom());
            currentUser.setAdresse(user.getAdresse());
            currentUser.setNumeroTelephone(user.getNumeroTelephone());
            if (currentUser.getEmail() != user.getEmail()) {
                currentUser.setEmail(user.getEmail());
                mailService.sendVerificationMail(currentUser);
                activitiesService.save("Confirmation d'email", ActivityCategory.EMAIL_CONF);
            }
            userRepository.save(currentUser);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error_email", "Cet email existe déjà.");
            return settings(model);
        }
        activitiesService.save("Changement des informations de profil", ActivityCategory.PROFILE_U);

        return settings(model);
    }

    @PostMapping("password")
    public String updatePassword(HttpServletRequest request, Model model) {
        String oldPassword = request.getParameter("old_password");
        String newPassword = request.getParameter("new_password");
        String newPasswordConf = request.getParameter("new_password_conf");

        if (!encoder.matches(oldPassword, authService.getCurrentUser().getPassword())) {
            model.addAttribute("error_password_old", "L'ancien mot de passe est incorrect");
            return settings(model);
        }

        if (!newPassword.equals(newPasswordConf)) {
            model.addAttribute("error_password_conf", "Les mots de passes ne sont pas identiques");
            return settings(model);
        }

        User currentUser = authService.getCurrentUser();
        currentUser.setPassword(encoder.encode(newPassword));
        userRepository.save(currentUser);

        model.addAttribute("success_password", "Le mot de passe a été changé.");
        activitiesService.save("Changement du mot de passe", ActivityCategory.PROFILE_PASS_CHANGE);

        return settings(model);

    }
}
