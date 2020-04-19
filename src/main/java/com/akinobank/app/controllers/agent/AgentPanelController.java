package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.MailService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/agent")
@Transactional
public class AgentPanelController {

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
    private MailService mailService;

    Logger logger = LoggerFactory.getLogger(AgentPanelController.class);



//    ************************************************* API Agent profile ************************************************************

    @GetMapping(value = "/profile/{id}") // return Agent by id
    public User getAgent(@PathVariable(value = "id")Long id) throws EntityNotFoundException {
        try{
            return agentRepository.getOne(id).getUser();
        }
        catch (EntityNotFoundException e){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "L'agent avec id = " + id + " est introuvable.");}

    }

    //    *********************************************************************************************************************
    //    *****************************************************API Agence profile **********************************************

    @GetMapping(value = "/agence")
    public Agence getAgence(){
        Long id = 2L; //just a test , we will use the token to get agent id
        Agent agent= agentRepository.findById(id).get();
        return agent.getAgence();
    }


    //    ************************************************************************************************************************
    //    ************************************************* API Show All clients ******************************************************


    @GetMapping(value = "/clients") //show all clients , works
    public List<User> getClients(){
        Agent agent = agentRepository.findById(1L).get(); // just for test : the idea is agent could see just his agence client
        return userRepository.findAllByRole(Role.CLIENT);
    }

    //    ******************************************************************************************************************
    //    ***************************************************** API ADD Client **********************************************


    @PostMapping("/clients/ajouter") //add new client , works
    public User addClient(@RequestBody  User user) throws DataIntegrityViolationException {
        try{
            user.setRole(Role.CLIENT);
        //find first the client
        Agent agent = agentRepository.findById(1L).get();  // i choose id 1 just for test
        userRepository.save(user); // add user in table
        Client client = Client.builder().agent(agent).agence(agent.getAgence()).user(user).build();
        clientRepository.save(client); // add client in table
        mailService.sendVerificationMailViaMG(user);

        return user;}
        catch (DataIntegrityViolationException | UnirestException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le mail vous avez entrée est deja existe !")  ;
        }
    }

    //    *********************************************************************************************************************
    //    ***************************************************** API Delete Client **********************************************


    @DeleteMapping(value = "/clients/{id}/supprimer") // delete a client , works
    public ResponseEntity<String> deleteClient(@PathVariable(value = "id") Long id){
        try{
            User user = userRepository.findById(id).get();
            userRepository.deleteById(id);
            clientRepository.delete(user.getClient());
            compteRepository.deleteAll(user.getClient().getComptes());
            return new ResponseEntity<>("Client est supprime avec succes." ,HttpStatus.OK);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }

    //    **********************************************************************************************************************
    //    ***************************************************** API Modify Client **********************************************


    @PutMapping(value = "/clients/{id}/modifier") // modify client , works
    public Serializable modifyClient(@PathVariable(name = "id") Long id , @RequestBody User user){
        logger.info("CLIENT ID = " + id);
        try {
            User userToModify = clientRepository.findById(id).get().getUser();
            if (user.getEmail() != null)
                userToModify.setEmail(user.getEmail());
            if (user.getNom() != null)
                userToModify.setNom(user.getNom());
            if (user.getPrenom() != null)
                userToModify.setPrenom(user.getPrenom());
            if (user.getNumeroTelephone() != null)
                userToModify.setNumeroTelephone(user.getNumeroTelephone());

            // renvoyer le code de confirmation pr verifier le nouveau email
            if (user.getEmail() != null && !user.getEmail().equals(userToModify.getEmail())) {
                userToModify.setEmailConfirmed(false);
                mailService.sendVerificationMailViaMG(user);
            }
            return userRepository.save(userToModify);
        }
        catch (NoSuchElementException | UnirestException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }

    //    **********************************************************************************************************************
    //    ******************************************************** API get Client Comptes***************************************

    @GetMapping(value = "/clients/{id}/comptes") // works v2
    public Collection<Compte> getAllComptes(@PathVariable(value = "id")Long id) throws NoSuchElementException {
        //Agents can see their clients in the same agence
        try {
            return clientRepository.findById(id).get().getComptes();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }

    //    **********************************************************************************************************************
    //    *********************************************************** API Add Client Compte *************************************

    @PostMapping(value = "/clients/{id}/comptes/ajouter") // works
    public Compte addClientCompte(@PathVariable(value = "id")Long id,@RequestBody Compte compte){ // PS : if you didnt insert the solde , will be auto 0.0 and always its >0
       try{ //check firstly if client exist
        Client client = clientRepository.findById(id).get();
        compte.setClient(client);
        return compteRepository.save(compte);}
       catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }

    //    **********************************************************************************************************************
    //    *********************************************************** API Delete Client Compte *********************************

    @DeleteMapping(value = "/comptes/{id}/supprimer") //works
    public ResponseEntity<String> deleteClientCompte(@PathVariable(value = "id") String numeroCompte){

        Agent agent = agentRepository.findById(1L).get();
        try {
            // verifier si le compte est de meme agence que l'agent
            Compte compte = compteRepository.findByClient_Agent_AgenceAndNumeroCompte(agent.getAgence(), numeroCompte).get();
            compteRepository.delete(compte);
            return new ResponseEntity<>("Client est supprimer avec succes." , HttpStatus.OK);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec le numero = " + numeroCompte + " est introuvable.");
        }
    }
    //    **********************************************************************************************************************
    //    *********************************************************** API modify Client Compte *********************************

        @PutMapping(value = "/comptes/{id}/modifer")
    public Compte modifyClientCompte(@PathVariable(value = "id") String numeroCompte ,@RequestBody Compte compte) {
            try {
            Compte compteToModify = compteRepository.findById(numeroCompte).get();
            if (compte.getIntitule() != null)
                compteToModify.setIntitule(compte.getIntitule());
            if (compte.getSolde() != 0.0)
                compteToModify.setSolde(compte.getSolde());

            return compteRepository.save(compteToModify);
        }  catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec le numero = " + numeroCompte + " est introuvable.");
        }
        }
}
