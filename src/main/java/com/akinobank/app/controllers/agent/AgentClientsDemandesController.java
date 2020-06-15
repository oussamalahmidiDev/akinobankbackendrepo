package com.akinobank.app.controllers.agent;

import com.akinobank.app.models.Demande;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.MailService;
import com.akinobank.app.services.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("/agent/api/clients/demandes")
@Transactional
@CrossOrigin(value = "*")
public class AgentClientsDemandesController {

    Logger logger = LoggerFactory.getLogger(AgentClientsDemandesController.class);

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

    @PutMapping(value = "/{id}/approve")
    public ResponseEntity<String> approveDemande (@PathVariable("id") Long id) {
        Demande demande = demandeRepository.findById(id).get();
        User clientUser = demande.getClient().getUser();

        if (demande.getEmail() != null && !demande.getEmail().equalsIgnoreCase(clientUser.getEmail())) {
            clientUser.setEmail(demande.getEmail());
            clientUser.setEmailConfirmed(false);
            mailService.sendVerificationMail(clientUser);
        }
        if (demande.getNom() != null)
            clientUser.setNom(demande.getNom());
        if (demande.getPrenom() != null)
            clientUser.setPrenom(demande.getPrenom());

        userRepository.save(clientUser);
        demandeRepository.delete(demande);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping()
    public List<Demande> getDemandes () {
        return  demandeRepository.findAll();

    }
}
