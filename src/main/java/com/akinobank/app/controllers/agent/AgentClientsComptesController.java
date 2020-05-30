package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.MailService;
import com.akinobank.app.services.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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




    @GetMapping(value = "/{id}/comptes") // works v2
    public Collection<Compte> getAllComptes(@PathVariable(value = "id")Long id) {
        try {
            return clientRepository.findById(id).get().getComptes();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }


    @PostMapping(value = "/{id}/comptes/ajouter") // works
    public Compte addClientCompte(@PathVariable(value = "id")Long id, @RequestBody AddCompteVerification addCompteVerification){
        Agent agent = agentProfileController.getAgent();
        Compte compte = new Compte();
        System.out.println(addCompteVerification);

        try { //check firstly if client exist
            if(!agent.getUser().getPassword().equals(addCompteVerification.getAgentPassword())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Mauvais mot de passe.");
            }
            compte.setIntitule(addCompteVerification.getIntitule());
            compte.setSolde(addCompteVerification.getSolde());

            Client client = clientRepository.findById(id).get();
            compte.setClient(client);
            compteRepository.save(compte);
            mailService.sendCompteDetails(client.getUser(), compte);

            //TODO check notificatiion class , there s some prb while parsing a new account for client
//            Notification notification = notificationRepository.save(Notification.builder()
//                    .client(compte.getClient())
//                    .contenu("Un <b>nouveau compte</b> à été ajouté ! Veuillez verifier votre e-mail pour récupérer votre code.")
//                    .build()
//            );
            return compte;
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }

    @GetMapping(value = "/{id}/comptes/{idComptes}") //works
    public Compte getClientCompteByNum(@PathVariable(value = "id") Long id , @PathVariable(value = "idComptes") String numeroCompte){
        Agent agent = agentProfileController.getAgent();
        System.out.println(id);
        Client client = agentClientsController.getOneClient(id);
        try {
//            String subNumero = numeroCompte.substring(8);
                Compte compte = compteRepository.findByClientAndClient_AgenceAndNumeroCompteContaining(client ,agent.getAgence(), numeroCompte).get();
            return compte;
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec le numero = " + numeroCompte + " est introuvable.");
        }
    }


    @DeleteMapping(value = "/{id}/comptes/{idComptes}/supprimer") //works
    public ResponseEntity<?> deleteClientCompte(@PathVariable(value = "id") Long id , @PathVariable(value = "idComptes") String numeroCompte){
        Agent agent = agentProfileController.getAgent();
        try {
            String subNumero = numeroCompte.substring(8);
            System.out.println(subNumero);
            Compte compte = compteRepository.findByClient_AgenceAndNumeroCompteContaining(agent.getAgence(), subNumero).get();
            compteRepository.delete(compte);

            Notification notification = notificationRepository.save(Notification.builder()
                    .client(compte.getClient())
                    .contenu("Le compte de nº <b>" + compte.getNumeroCompteHidden() + "</b> à été supprimé.")
                    .build()
            );

            return new ResponseEntity<>("Le compte est supprime avec succes." , HttpStatus.OK);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec le numero = " + numeroCompte + " est introuvable.");
        }
    }


    @PutMapping(value = "/{id}/comptes/{idComptes}/modifier")
    public Compte modifyClientCompte(@PathVariable(value = "id") Long id ,
                                     @PathVariable(value = "idComptes") String numeroCompte ,
                                     @RequestBody Compte compte) {
        try {
//            Compte compteToModify = compteRepository.findById(numeroCompte).get();
            Compte compteToModify = getClientCompteByNum(id,numeroCompte) ;// just for test
            System.out.println(compte.getStatut());
            if (compte.getIntitule() != null)
                compteToModify.setIntitule(compte.getIntitule());
            if (compte.getSolde() != 0.0)
                compteToModify.setSolde(compte.getSolde());
            if (compte.getStatut() != null && compte.getStatut().equals("BLOQUER")){
                System.out.println(compte.getStatut());
//                compteToModify.setStatut(CompteStatus.BLOCKED);
                }
//                compteToModify.setStatut(compte.getStatut());

            //TODO notification need to be fixed

            Notification notification = notificationRepository.save(Notification.builder()
                    .client(compte.getClient())
                    .contenu("Le compte de nº <b>" + compte.getNumeroCompteHidden() + "</b> à été modifié par votre agent.")
                    .build()
            );

            return compteRepository.save(compteToModify);
        }  catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec le numero = " + numeroCompte + " est introuvable.");
        }
    }

}
