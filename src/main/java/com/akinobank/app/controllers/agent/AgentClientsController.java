package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.ChangeClientDataRequest;
import com.akinobank.app.models.Client;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.MailService;
import com.akinobank.app.services.UploadService;
import com.akinobank.app.utilities.VerificationTokenGenerator;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/agent/api/clients")
@Transactional
@CrossOrigin(value = "*")
public class AgentClientsController {

    Logger logger = LoggerFactory.getLogger(AgentClientsController.class);

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
    private PasswordEncoder encoder;


    @GetMapping() //show all clients , works
    public List<User> getClients(){
//        return clientRepository.findAll();
        return userRepository.findAllByRoleAndArchived(Role.CLIENT,false);

//        return userRepository.findAllByRole(Role.CLIENT);
    }



    @GetMapping(value="/{id}")
    public Client getOneClient(@PathVariable(value = "id")Long id) {
        try{
            return clientRepository.findById(id).get();
        } catch (NoSuchElementException | EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable")  ;
        }
    }

    @GetMapping(value="/rechercher/{nom}")
    public List<User> getClientByName(@PathVariable(value = "nom") String clientName) {
        try{
            return userRepository.findUserByRoleAndNom(Role.CLIENT, clientName);
        } catch (NoSuchElementException | EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec nom = " + clientName + " est introuvable")  ;
        }
    }


    @PostMapping("/ajouter") //add new client , works
    public User addClient(@RequestBody User user) {
        try {
            user.setRole(Role.CLIENT);
            Agent agent = agentProfileController.getAgent();

            userRepository.save(user);

            Client client = Client.builder()
                    .agence(agent.getAgence())
                    .user(user)
                    .build();

            clientRepository.save(client);
            mailService.sendVerificationMail(user);
            return user;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'email que vous avez entré est déjà utilisé."+e.toString())  ;
        }
    }

    @DeleteMapping(value = "/{id}/supprimer") // delete a client , works
    public ResponseEntity<?> deleteClient(@PathVariable(value = "id") Long id){
        try {
            Client client = userRepository.findById(id).get().getClient();
//            userRepository.delete(client.getUser());
//            clientRepository.delete(client);
//            compteRepository.deleteAll(client.getComptes());
            client.getUser().setArchived(true);

            return new ResponseEntity<>("Client est supprime avec succes." ,HttpStatus.OK);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }


    @PutMapping(value = "/{id}/modifier") // modify client , works
    public User modifyClient(@PathVariable(value = "id") Long id ,
                             @RequestBody ChangeClientDataRequest changeClientDataRequest) throws UnirestException {
        logger.info("CLIENT ID = " + id);

        try {
            Agent agent = agentProfileController.getAgent();
            User user = changeClientDataRequest.getUser();
            User userToModify = clientRepository.findById(changeClientDataRequest.getUser().getId()).get().getUser();
            Boolean isMatch = encoder.matches(user.getPassword(),changeClientDataRequest.getAgentPassword());
            if(!isMatch){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Mauvais mot de passe.");
            }
            if (user.getEmail() != null)
                userToModify.setEmail(user.getEmail());
            if (user.getNom() != null)
                userToModify.setNom(user.getNom());
            if (user.getPrenom() != null)
                userToModify.setPrenom(user.getPrenom());
            if (user.getNumeroTelephone() != null)
                userToModify.setNumeroTelephone(user.getNumeroTelephone());
            if(user.getVille() != null){
                userToModify.setVille(user.getVille());
            }
            if(user.getAdresse() != null){
                userToModify.setAdresse(user.getAdresse());
            }

            // renvoyer le code de confirmation pr verifier le nouveau email

            if (user.getEmail() != null && !user.getEmail().equals(userToModify.getEmail())) {
                userToModify.setEmailConfirmed(false);
                mailService.sendVerificationMail(user);
            }
            return userRepository.save(userToModify);
        }
            catch (NoSuchElementException e){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
            }
    }

    @PutMapping(value = "/{id}/modifier/contact") // modify client , works
    public User modifyClient(@PathVariable(value = "id") Long id , @RequestBody User user) throws UnirestException {
        logger.info("CLIENT ID = " + id);

        try {
            User userToModify = clientRepository.findById(id).get().getUser();
            if(user.getVille() != null){
                userToModify.setVille(user.getVille());
            }
            if(user.getAdresse() != null){
                userToModify.setAdresse(user.getAdresse());
            }
            // renvoyer le code de confirmation pr verifier le nouveau email

            if (user.getEmail() != null && !user.getEmail().equals(userToModify.getEmail())) {
                userToModify.setEmailConfirmed(false);
                mailService.sendVerificationMail(user);
            }
            return userRepository.save(userToModify);
        }
        catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }

    @PostMapping(value="/{id}/verification")
    public ResponseEntity sendClientVerification(@PathVariable(value = "id") Long  id ,
                                                    @RequestBody ChangeClientDataRequest changeClientDataRequest) {
        System.out.println(changeClientDataRequest);
        HashMap<String, String> map = new HashMap<>();
        try{
            if(!agentProfileController.getAgent().getUser().getPassword().equals(changeClientDataRequest.getAgentPassword())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Mauvais mot de passe.");
            }
            User user = getOneClient(id).getUser();
            user.setPassword(null);
            user.setVerificationToken(VerificationTokenGenerator.generateVerificationToken());
            userRepository.save(user);
            map.put("text","La vérification a été envoyée avec succès.");

            mailService.sendVerificationMail(user);
            return new ResponseEntity(map,HttpStatus.OK);
        } catch (NoSuchElementException | EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }

    @GetMapping("/avatar/{filename}")
    public ResponseEntity<Resource> getCLientAvatar(HttpServletRequest request, @PathVariable("filename") String filename) {
//        Client client = getAgent().getAgence().getClients();
        System.out.println(filename);

//        if (agent.getUser().getPhoto() == null )
//            throw new ResponseStatusException(HttpStatus.OK, "Pas de photo définie.");


        Resource resource = uploadService.get(filename);

        // setting content-type header
        String contentType = null;
        try {
            // setting content-type header according to file type
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Type indéfini.");
        }
        // setting content-type header to generic octet-stream
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
