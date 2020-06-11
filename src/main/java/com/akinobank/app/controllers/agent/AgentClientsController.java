package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.ChangeClientDataRequest;
import com.akinobank.app.models.Client;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.MailService;
import com.akinobank.app.services.UploadService;
import com.akinobank.app.utilities.VerificationTokenGenerator;
import com.mashape.unirest.http.exceptions.UnirestException;
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

    @Autowired
    private ActivitiesService activitiesService;


    @GetMapping() //show all clients , works
    public List<Client> getClients() {
        return clientRepository.findAll();
    }


    @GetMapping(value = "/{id}")
    public Client getOneClient(@PathVariable(value = "id") Long id) {
        return clientRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ce client est introuvable.")
        );
    }

    @GetMapping(value = "/rechercher/{nom}")
    public List<User> getClientByName(@PathVariable(value = "nom") String clientName) {
        try {
            return userRepository.findUserByRoleAndNom(Role.CLIENT, clientName);
        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec nom = " + clientName + " est introuvable");
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
                .agent(agent)
                .user(user)
                .build();

            clientRepository.save(client);
            mailService.sendVerificationMail(user);

            activitiesService.save(
                String.format("Création d'un nouveau client \"%s %s\" dans l'agence %s (%s)", client.getUser().getNom(), client.getUser().getNom(), client.getAgence().getLibelleAgence(), client.getAgence().getVille().getNom()),
                ActivityCategory.CLIENTS_C
            );

            return user;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'email que vous avez entré est déjà utilisé.");
        }
    }

    @DeleteMapping(value = "/{id}/supprimer") // delete a client , works
    @ResponseStatus(value = HttpStatus.OK, reason = "Le client a été supprimé avec succès.")
    public void deleteClient(@PathVariable(value = "id") Long id) {
        Client client = getOneClient(id);
        client.getUser().setArchived(true);

        activitiesService.save(
            String.format("Suppression du client \"%s %s\" dans l'agence %s (%s)", client.getUser().getNom(), client.getUser().getNom(), client.getAgence().getLibelleAgence(), client.getAgence().getVille().getNom()),
            ActivityCategory.CLIENTS_D
        );
    }


    @PutMapping(value = "/{id}/modifier") // modify client , works
    public User modifyClient(@PathVariable(value = "id") Long id, @RequestBody ChangeClientDataRequest changeClientDataRequest) {
        logger.info("CLIENT ID = " + id);

        Agent agent = agentProfileController.getAgent();

        if (!encoder.matches(changeClientDataRequest.getAgentPassword(), agent.getUser().getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mauvais mot de passe.");

        User requestUser = changeClientDataRequest.getUser();
        User userToModify = getOneClient(id).getUser();

        userToModify.setNom(requestUser.getNom());
        userToModify.setPrenom(requestUser.getPrenom());
        userToModify.setNumeroTelephone(requestUser.getNumeroTelephone());

        if (!requestUser.getEmail().equals(userToModify.getEmail())) {
            userToModify.setEmailConfirmed(false);
            userToModify.setEmail(requestUser.getEmail());
            mailService.sendVerificationMail(userToModify);
        }

        userRepository.save(userToModify);

        activitiesService.save(
            String.format("Modification des informations du client \"%s %s\"", userToModify.getNom(), userToModify.getPrenom()),
            ActivityCategory.CLIENTS_U
        );

        return userToModify;

    }

    @PutMapping(value = "/{id}/modifier/contact") // modify client , works
    public User modifyClient(@PathVariable(value = "id") Long id, @RequestBody User body) throws UnirestException {
        logger.info("CLIENT ID = " + id);
        User userToModify = getOneClient(id).getUser();

        userToModify.setVille(body.getVille());
        userToModify.setAdresse(body.getAdresse());

        activitiesService.save(
            String.format("Modification de l'adresse postale du client \"%s %s\"", userToModify.getNom(), userToModify.getPrenom()),
            ActivityCategory.CLIENTS_U
        );

        return userRepository.save(userToModify);
    }

    @PostMapping(value = "/{id}/verification")
    public ResponseEntity sendClientVerification(@PathVariable(value = "id") Long id,
                                                 @RequestBody ChangeClientDataRequest changeClientDataRequest) {
        System.out.println(changeClientDataRequest);
        HashMap<String, String> map = new HashMap<>();
        try {
            if (!agentProfileController.getAgent().getUser().getPassword().equals(changeClientDataRequest.getAgentPassword())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mauvais mot de passe.");
            }
            User user = getOneClient(id).getUser();
            user.setPassword(null);
            user.setVerificationToken(VerificationTokenGenerator.generateVerificationToken());
            userRepository.save(user);
            map.put("text", "La vérification a été envoyée avec succès.");

            mailService.sendVerificationMail(user);
            return new ResponseEntity(map, HttpStatus.OK);
        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }

    @GetMapping("/avatar/{filename}")
    public ResponseEntity<Resource> getCLientAvatar(HttpServletRequest request, @PathVariable("filename") String filename) {
        System.out.println(filename);

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
