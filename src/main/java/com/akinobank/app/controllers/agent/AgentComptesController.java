package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.enumerations.NotificationType;
import com.akinobank.app.models.Compte;
import com.akinobank.app.models.Notification;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.repositories.NotificationRepository;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.MailService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/agent/api/comptes")
@RestController
@Log4j2
public class AgentComptesController {

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ActivitiesService activitiesService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private AuthService authService;

    @GetMapping("{id}")
    public Compte getCompteById(@PathVariable String id) {
        log.info("GET ALL COMPTEs");
        return compteRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec nº " + id + " est introuvable.")
        );
    }

    @DeleteMapping("{id}/supprimer")
    @ResponseStatus(HttpStatus.OK)
    public void deleteCompte(@PathVariable String id) {
        compteRepository.delete(getCompteById(id));

        activitiesService.save(
            String.format("Suppression définitive du compte nº %s", id),
            ActivityCategory.COMPTES_D
        );
    }

    @PutMapping("{id}/modifier")
    public Compte updateCompte(@PathVariable String id, @RequestBody Compte body) {
        Compte compteToUpdate = getCompteById(id);
        compteToUpdate.setSolde(body.getSolde());
        compteToUpdate.setIntitule(body.getIntitule());

        compteRepository.save(compteToUpdate);

        activitiesService.save(
            String.format("Modification des informations du compte nº %s", compteToUpdate.getNumeroCompte()),
            ActivityCategory.COMPTES_U
        );

        return compteToUpdate;
    }

    @PostMapping("{id}/resend_infos")
    @ResponseStatus(HttpStatus.OK)
    public void resendInfo(@PathVariable("id")String id){

        Compte compte = getCompteById(id);
        mailService.sendCompteDetails(compte.getClient().getUser(), compte);

    }



    @PutMapping("{id}/modifier/status")
    public Compte updateCompteStatus(@PathVariable String id, @RequestParam(value = "status") String status) {
        Compte compteToUpdate = getCompteById(id);
        compteToUpdate.setStatut(CompteStatus.valueOf(status));
        compteRepository.save(compteToUpdate);

        activitiesService.save(
            String.format("Modification de statut du compte nº %s. Le nouveau statut : %s", compteToUpdate.getNumeroCompte(), compteToUpdate.getStatut().name()),
            ActivityCategory.COMPTES_U
        );

        List<User> receivers = new ArrayList<>();
        receivers.add(compteToUpdate.getClient().getUser());

//        Notification notification = Notification.builder().
//            .receiver(receivers)
//            .type(NotificationType.SUCCESS)
//            .build();

//        if (compteToUpdate.getStatut().equals(CompteStatus.BLOCKED))
//            notification.setContenu(String.format("Le compte nº %s a été bloqué par l'agent %s %s suivant votre demande.", compteToUpdate.getNumeroCompte(), authService.getCurrentUser().getPrenom(), authService.getCurrentUser().getNom()));
//        else if (compteToUpdate.getStatut().equals(CompteStatus.SUSPENDED))
//            notification.setContenu(String.format("Le compte nº %s a été suspendu par l'agent %s %s suivant votre demande.", compteToUpdate.getNumeroCompte(), authService.getCurrentUser().getPrenom(), authService.getCurrentUser().getNom()));
//
//        notificationRepository.save(notification);

        return compteToUpdate;
    }

}
