package com.akinobank.app.services;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Activity;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ActivitiesService {

    private Map<Role, HashMap<ActivityCategory, String>> categories;

    @Autowired
    private ActivityRepository repository;

    @Autowired
    private AuthService authService;

    public ActivitiesService() {

        categories = new HashMap<>();

        HashMap<ActivityCategory, String> adminCategories = new HashMap<>();
        adminCategories.put(ActivityCategory.USERS_C, "Création d'un utilisateur");
        adminCategories.put(ActivityCategory.USERS_U, "Modification d'un utilisateur");
        adminCategories.put(ActivityCategory.USERS_D, "Suppression d'un utilisateur");

        adminCategories.put(ActivityCategory.AGENCES_C, "Création d'une agence");
        adminCategories.put(ActivityCategory.AGENCES_U, "Modification d'une agence");
        adminCategories.put(ActivityCategory.AGENCES_D, "Suppression d'une agence");
        adminCategories.put(ActivityCategory.AUTH, "Authentification d'un admin");

        categories.put(Role.ADMIN, adminCategories);

        HashMap<ActivityCategory, String> agentCategories = new HashMap<>();
        agentCategories.put(ActivityCategory.CLIENTS_C, "Création d'un client");
        agentCategories.put(ActivityCategory.CLIENTS_U, "Modification d'un client");
        agentCategories.put(ActivityCategory.CLIENTS_D, "Suppression d'un client");

        agentCategories.put(ActivityCategory.COMPTES_C, "Création d'un compte");
        agentCategories.put(ActivityCategory.COMPTES_U, "Modification d'un compte");
        agentCategories.put(ActivityCategory.COMPTES_D, "Suppression d'un compte");
        agentCategories.put(ActivityCategory.COMPTES_ACTIVATE, "Activation d'un compte");
        agentCategories.put(ActivityCategory.COMPTES_BLOCK, "Blocage d'un compte");
        agentCategories.put(ActivityCategory.COMPTES_SUSPEND, "Suspension d'un compte");
        agentCategories.put(ActivityCategory.AUTH, "Authentification d'un agent");


        categories.put(Role.AGENT, agentCategories);

        HashMap<ActivityCategory, String> clientCategories = new HashMap<>();
        clientCategories.put(ActivityCategory.PROFILE_U, "Changement des informations de profil");
        clientCategories.put(ActivityCategory.PROFILE_PASS_CHANGE, "Changement du mot de passe");
        clientCategories.put(ActivityCategory.COMPTES_CODE_CHANGE, "Changement du code secret de compte");
        clientCategories.put(ActivityCategory.COMPTES_DEMANDE_SUSPEND, "Envoie d'une demande de supsension de compte");
        clientCategories.put(ActivityCategory.COMPTES_DEMANDE_BLOCK, "Envoie d'une demande de blocage de compte");
        clientCategories.put(ActivityCategory.VIREMENTS_C, "Effectue d'un virement");
        clientCategories.put(ActivityCategory.VIREMENTS_CONF, "Confirmation d'un virement");
        clientCategories.put(ActivityCategory.VIREMENTS_D, "Suppression d'un virement");
        clientCategories.put(ActivityCategory.RECHARGES_C, "Effectue d'une recharge");
        clientCategories.put(ActivityCategory.RECHARGE_D, "Suppression d'une recharge");
        clientCategories.put(ActivityCategory.AUTH, "Authentification d'un client");

        categories.put(Role.CLIENT, clientCategories);
    }

    public void save(String evenement, ActivityCategory category) {
        Activity activity = new Activity();

        activity.setUser(authService.getCurrentUser());
        activity.setEvenement(evenement);
        activity.setCategory(categories.get(authService.getCurrentUser().getRole()).get(category));

        repository.save(activity);
    }

    public void save(ActivityCategory category) {
        Activity activity = new Activity();

        activity.setUser(authService.getCurrentUser());
        activity.setEvenement("Pas indiqué");
        activity.setCategory(categories.get(authService.getCurrentUser().getRole()).get(category));

        repository.save(activity);
    }

    public void save(String evenement, ActivityCategory category, User user) {
        Activity activity = new Activity();

        activity.setUser(user);
        activity.setEvenement(evenement);
        activity.setCategory(categories.get(user.getRole()).get(category));

        repository.save(activity);
    }

}
