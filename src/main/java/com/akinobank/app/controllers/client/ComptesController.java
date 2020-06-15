package com.akinobank.app.controllers.client;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.AgentRepository;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.repositories.NotificationRepository;
import com.akinobank.app.services.ActivitiesService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/client/api/comptes")
@Log4j2
public class ComptesController {
    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ClientProfileController profileController;

    @Autowired
    private ActivitiesService activitiesService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AgentRepository agentRepository;


    @GetMapping()
    public Collection<Compte> getClientComptes() {
        return profileController.getClient().getComptes();
    }

    //    ** API to change code secret ***
    @PostMapping("/changer_code")
    public Compte changeCodeSecret(@RequestBody @Valid CodeChangeRequest request) {
        Client client = profileController.getClient();
        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), client).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le compte est introuvable")
        );

        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "L'ancien code est incorrect.");

        if (!request.getNewCodeSecret().equals(request.getNewCodeSecretConf()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Les nouveaux codes ne sont pas identiques.");

        compte.setCodeSecret(request.getNewCodeSecret());
        compteRepository.save(compte);

        activitiesService.save(
            String.format("Changement du code secret de compte nº %s", compte.getNumeroCompte()),
            ActivityCategory.COMPTES_CODE_CHANGE
        );
        return compte;
    }

    @PostMapping(value = "/verify_number")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void verifyCompteNumber(@RequestBody CompteCredentialsRequest request, @RequestParam(value = "operation") String operation) {
        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), profileController.getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );
        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");
        if (operation.equals("change_status") || operation.equals("change_code"))
            verifyCompteStatus(compte, true);
        else
            verifyCompteStatus(compte, false);
    }

    @PutMapping(value = "/block")
    public ResponseEntity<Compte> compteBlock(@RequestBody Compte request) {
        Client client = profileController.getClient();

        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), client).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );
        if (compte.getStatut().equals(CompteStatus.BLOCKED) || compte.getStatut().equals(CompteStatus.PENDING_BLOCKED))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le compte est déjà bloqué.");
        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

        compte.setStatut(CompteStatus.PENDING_BLOCKED);
        compte.setRaison(request.getRaison());

        compteRepository.save(compte);

        activitiesService.save(
            String.format("Envoie d'une demande de blocage du compte nº %s pour la raison : %s", compte.getNumeroCompte(), request.getRaison()),
            ActivityCategory.COMPTES_DEMANDE_BLOCK
        );

        List<User> receivers = agentRepository.findAllByAgence(client.getAgence()).stream()
            .map(agent -> agent.getUser()).collect(Collectors.toList());

        receivers.forEach(user -> {
            log.info("Sending compte notification to : {}", user.getEmail());
        });

        Notification notification = Notification.builder()
            .receiver(receivers)
            .contenu(String.format("Le client \"%s %s\" a envoyé une demande de blocage de son compte", client.getUser().getNom(), client.getUser().getPrenom()))
            .build();

        notificationRepository.save(notification);


        return new ResponseEntity<>(compte, HttpStatus.OK);
    }

    @PutMapping(value = "/suspend")
    public ResponseEntity<Compte> compteSuspend(@RequestBody Compte request) {
        Client client = profileController.getClient();

        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), client).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );

        if (compte.getStatut().equals(CompteStatus.BLOCKED) || compte.getStatut().equals(CompteStatus.PENDING_SUSPENDED))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le compte est bloqué.");


        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

        compte.setStatut(CompteStatus.PENDING_SUSPENDED);
        compte.setRaison(request.getRaison());

        compteRepository.save(compte);

        activitiesService.save(
            String.format("Envoie d'une demande de suspension du compte nº %s pour la raison : %s", compte.getNumeroCompte(), request.getRaison()),
            ActivityCategory.COMPTES_DEMANDE_SUSPEND
        );

        List<User> receivers = agentRepository.findAllByAgence(client.getAgence()).stream()
            .map(agent -> agent.getUser()).collect(Collectors.toList());

        receivers.forEach(user -> {
            log.info("Sending compte notification to : {}", user.getEmail());
        });

        Notification notification = Notification.builder()
            .receiver(receivers)
            .contenu(String.format("Le client \"%s %s\" a envoyé une demande de suspsension de son compte", client.getUser().getNom(), client.getUser().getPrenom()))
            .build();

        notificationRepository.save(notification);

        return new ResponseEntity<>(compte, HttpStatus.OK);
    }

    // helper function to check if Compte is active.
    public void verifyCompteStatus(Compte compte, boolean allow) {
        if (!compte.getStatut().name().equals(CompteStatus.ACTIVE.name()) && !allow)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Ce compte n'est pas actif.");
    }


}
