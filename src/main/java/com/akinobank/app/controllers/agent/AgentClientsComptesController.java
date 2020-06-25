package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.MailService;
import com.akinobank.app.services.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/agent/api/clients")
@Transactional
@CrossOrigin(value = "*")
public class AgentClientsComptesController {

    Logger logger = LoggerFactory.getLogger(AgentClientsComptesController.class);

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgenceRepository agenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private MailService mailService;

    @Autowired
    private AgentProfileController agentProfileController;

    @Autowired
    private AgentClientsController agentClientsController;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ActivitiesService activitiesService;


    @GetMapping(value = "/{id}/comptes") // works v2
    public Collection<Compte> getAllComptes(@PathVariable(value = "id") Long id) {
        try {
            return clientRepository.findByIdAndAgence(id, agentProfileController.getAgent().getAgence()).get().getComptes();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }


    @PostMapping(value = "/{id}/comptes/ajouter") // works
    public Compte addClientCompte(@PathVariable(value = "id") Long id, @RequestBody CompteRequest compteRequest) {
        Agent agent = agentProfileController.getAgent();
        Compte compte = new Compte();
        Boolean isMatch = encoder.matches(compteRequest.getAgentPassword(), agent.getUser().getPassword());
        try { //check firstly if client exist
            if (!isMatch) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mauvais mot de passe.");
            }
            compte.setIntitule(compteRequest.getIntitule());
            compte.setSolde(compteRequest.getSolde());

            Client client = clientRepository.findByIdAndAgence(id,agent.getAgence()).get();
            compte.setClient(client);
            compteRepository.save(compte);
            mailService.sendCompteDetails(client.getUser(), compte);

            activitiesService.save(
                String.format("Création d'un nouveau compte nº %s pour le client %s %s avec l'intitulé %s et de montant %.2f", compte.getNumeroCompte(), client.getUser().getNom(), client.getUser().getPrenom(), compte.getIntitule(), compte.getSolde()),
                ActivityCategory.COMPTES_C
            );


            return compte;
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }

//    @GetMapping(value = "/{id}/comptes/{idComptes}") //works
//    public Compte getClientCompteByNum(@PathVariable(value = "id") Long id, @PathVariable(value = "idComptes") String numeroCompte) {
//        Agent agent = agentProfileController.getAgent();
//        Client client = clientRepository.findByIdAndAgence(id,agent.getAgence()).get();
//        try {
//            Compte compte = compteRepository.findByClientIdAndNumeroCompte(client.getId(),numeroCompte);
//            return compte;
//        } catch (NoSuchElementException e) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec le numero = " + numeroCompte + " est introuvable.");
//        }
//    }


//    @DeleteMapping(value = "/{id}/comptes/{idComptes}/supprimer") //works
//    public ResponseEntity<?> deleteClientCompte(@PathVariable(value = "id") Long id, @PathVariable(value = "idComptes") String numeroCompte) {
//        Agent agent = agentProfileController.getAgent();
//        Client client = clientRepository.findByIdAndAgence(id,agent.getAgence()).get();
//        try {
//            Compte compte = compteRepository.findByClientIdAndNumeroCompte(client.getId(), numeroCompte);
//            compteRepository.delete(compte);
//
//            Notification notification = notificationRepository.save(Notification.builder()
//                    .contenu("Le compte de nº <b>" + compte.getNumeroCompteHidden() + "</b> à été supprimé.")
//                    .build()
//            );
//            return new ResponseEntity<>("Le compte est supprime avec succes.", HttpStatus.OK);
//        } catch (NoSuchElementException e) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec le numero = " + numeroCompte + " est introuvable.");
//        }
//    }


//    @PutMapping(value = "/{id}/comptes/{idComptes}/modifier")
//    public Compte modifyClientCompte(@PathVariable(value = "id") Long id,
//                                     @PathVariable(value = "idComptes") String numeroCompte,
//                                     @RequestBody Compte compte) {
//        try {
//            Compte compteToModify = compteRepository.findById(numeroCompte).get();
////            Client client = clientRepository.findById(id).get();
////            Compte compteToModify = getClientCompteByNum(id,numeroCompte) ;// just for test
//            if (compte.getIntitule() != null)
//                compteToModify.setIntitule(compte.getIntitule());
//            if (compte.getSolde() != 0.0)
//                compteToModify.setSolde(compte.getSolde());
//
//            Notification notification = notificationRepository.save(Notification.builder()
//                    .contenu("Le compte de nº <b>" + compteToModify.getNumeroCompteHidden() + "</b> à été modifié par votre agent.")
//                    .build()
//            );
//
//            return compteRepository.save(compteToModify);
//        } catch (NoSuchElementException e) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec le numero = " + numeroCompte + " est introuvable.");
//        }
//    }

//    @PutMapping(value = "/{id}/comptes/{idComptes}/modifier/status")
//    public Compte modifyClientCompteStatus(@PathVariable(value = "id") Long id,
//                                           @PathVariable(value = "idComptes") String numeroCompte,
//                                           @RequestParam(value = "status") String status) {
//        try {
//            Compte compteToModify = compteRepository.findById(numeroCompte).get();
//            switch (status) {
//                case "activer":
//                    compteToModify.setStatut(CompteStatus.ACTIVE);
//                    break;
//                case "bloquer":
//                    compteToModify.setStatut(CompteStatus.BLOCKED);
//                    break;
//                case "suspendre":
//                    compteToModify.setStatut(CompteStatus.SUSPENDED);
//                    break;
//            }
//            compteRepository.save(compteToModify);
//
//
//            Notification notification = notificationRepository.save(Notification.builder()
//                    .contenu("Le compte de nº <b>" + compteToModify.getNumeroCompteHidden() + "</b> à été modifié par votre agent.")
//                    .build()
//            );
//            return compteToModify;
//        } catch (NoSuchElementException e) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec le numero = " + numeroCompte + " est introuvable.");
//        }
//    }

}
