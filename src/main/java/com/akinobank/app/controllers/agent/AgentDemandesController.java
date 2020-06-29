package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.Compte;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.repositories.NotificationRepository;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.AuthService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/agent/api/demandes")
@CrossOrigin(value = "*")
@Log4j2
public class AgentDemandesController {

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ActivitiesService activitiesService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private AgentProfileController agentProfileController;

    @Autowired
    private AgentComptesController agentComptesController;

    @PostMapping("/accept")
    public Compte acceptDemande(@RequestParam("numeroCompte")String numeroCompte){
        Agent agent = agentProfileController.getAgent();
        try{
            Compte compte = compteRepository.findByNumeroCompteAndClient_Agence(numeroCompte,agent.getAgence()).get();
            switch(compte.getStatut()){
                case PENDING_ACTIVE:
                    compte.setStatut(CompteStatus.ACTIVE);
                    break;
                case PENDING_BLOCKED:
                    compte.setStatut(CompteStatus.BLOCKED);
                    break;
                case PENDING_SUSPENDED:
                    compte.setStatut(CompteStatus.SUSPENDED);
                    break;
            }
//            compte.getClient().setNumberOfDemandes(compte.getClient().getNumberOfDemandes() - 1);
            compteRepository.save(compte);

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
            compte.setStatut(compte.getOldStatut());
//            compte.getClient().setNumberOfDemandes(compte.getClient().getNumberOfDemandes() - 1);
            compteRepository.save(compte);

            return compte;
        }catch(Exception e){
            throw new  ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec nº " + numeroCompte + " est introuvable.");
        }
    }


}
