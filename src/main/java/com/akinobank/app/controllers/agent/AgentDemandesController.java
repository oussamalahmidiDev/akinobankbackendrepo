package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.Compte;
import com.akinobank.app.models.Notification;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.services.NotificationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/agent/api/demandes")
@CrossOrigin(value = "*")
@Log4j2
public class AgentDemandesController {

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AgentProfileController agentProfileController;

    @PostMapping("/accept")
    public Compte acceptDemande(@RequestParam("numeroCompte")String numeroCompte){
        Agent agent = agentProfileController.getAgent();
        try{
            Compte compte = compteRepository.findByNumeroCompteAndClient_Agence(numeroCompte,agent.getAgence()).get();
            Notification notification = new Notification();
            switch(compte.getStatut()){
                case PENDING_ACTIVE:
                    compte.setStatut(CompteStatus.ACTIVE);
                    notification.setContenu("Votre demande d'activation de compte nº " + numeroCompte + " a été acceptée");
                    break;
                case PENDING_BLOCKED:
                    compte.setStatut(CompteStatus.BLOCKED);
                    notification.setContenu("Votre demande de blocage de compte nº " + numeroCompte + " a été acceptée");
                    break;
                case PENDING_SUSPENDED:
                    compte.setStatut(CompteStatus.SUSPENDED);
                    notification.setContenu("Votre demande de supension de compte nº " + numeroCompte + " a été acceptée");
                    break;
            }
//            compte.getClient().setNumberOfDemandes(compte.getClient().getNumberOfDemandes() - 1);
            compteRepository.save(compte);

            notificationService.send(notification, compte.getClient().getUser());

            return compte;

        }catch(Exception e){
            throw new  ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec nº " + numeroCompte + " est introuvable.");
        }
    }
    @PostMapping("/reject")
    public Compte rejectDemande(@RequestParam("numeroCompte")String numeroCompte ){
        Agent agent = agentProfileController.getAgent();
        try{
            Compte compte = compteRepository.findByNumeroCompteAndClient_Agence(numeroCompte,agent.getAgence()).get();
            Notification notification = new Notification();

            switch(compte.getStatut()){
                case PENDING_ACTIVE:
                    notification.setContenu("Votre demande d'activation de compte nº " + numeroCompte + " a été rejetée");
                    break;
                case PENDING_BLOCKED:
                    notification.setContenu("Votre demande de blocage de compte nº " + numeroCompte + " a été rejetée");
                    break;
                case PENDING_SUSPENDED:
                    notification.setContenu("Votre demande de supension de compte nº " + numeroCompte + " a été rejetée");
                    break;
            }
            compte.setStatut(compte.getOldStatut());

            compteRepository.save(compte);

            notificationService.send(notification, compte.getClient().getUser());
            return compte;
        }catch(Exception e){
            throw new  ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec nº " + numeroCompte + " est introuvable.");
        }
    }


}
