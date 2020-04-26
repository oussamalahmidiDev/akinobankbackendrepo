package com.akinobank.app.controllers.client;


import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.enumerations.VirementStatus;
import com.akinobank.app.exceptions.ResponseException;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.MailService;
import com.akinobank.app.services.NotificationService;
import com.akinobank.app.services.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Flux;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/client/api")
public class ClientPanelController {

    Logger logger = LoggerFactory.getLogger(ClientPanelController.class);

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private VirementRepository virementRepository;

    @Autowired
    private RechargeRepository rechargeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthService authService;


    //    ***************** API Client profile ********************

    @GetMapping(value = "/profile/{id}") // return Client by id
    public User getClient(@PathVariable("id") Long id) {
        return clientRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.")
        ).getUser();
    }

    //*****************************
    //******* API to modify Client Info *************

    @PostMapping(value = "/profile/changer")
    public Demande sendChangeDemande(@RequestBody Demande demande) {
        Client client = authService.getCurrentUser().getClient();

        logger.info("CURRENT CLIENT ID = {}", client.getId());
        Demande demandeFromDB = demandeRepository.findByClient(client);
        if (demandeFromDB != null)
            demande.setId(demandeFromDB.getId());

        demande.setClient(client);

        return demandeRepository.save(demande);
    }

//    ***************************************
//    ***************** API Client comptes ********************

    @GetMapping(value = "/{id}/comptes")// return listes des comptes d'un client
    public Collection<Compte> getClientComptes(@PathVariable(value = "id") Long id) {
        return Optional.of(compteRepository.findAllByClientId(id)).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.")
        );
    }

    //    ** API to change code secret ***
    @PostMapping("/comptes/management/changer_code")
    public Compte changeCodeSecret(@RequestBody @Valid CodeChangeRequest request) {
        Compte compte = compteRepository.findById(request.getNumeroCompte()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le compte est introuvable")
        );

        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "L'ancien code est incorrect.");

        if (!request.getNewCodeSecret().equals(request.getNewCodeSecretConf()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Les nouveaux codes ne sont pas identiques.");

        compte.setCodeSecret(request.getNewCodeSecret());
        return compteRepository.save(compte);

    }

//    ***************************************
    //    ***************** API Client Recharges ********************

    @GetMapping(value = "/{id}/recharges")// return listes des recharges  d'un client
    public Collection<Recharge> getAllRecharges(@PathVariable(value = "id") long id) {
        try {
            Collection<Compte> comptes = getClientComptes(id);
            Collection<Recharge> recharges = new ArrayList<>();

            for (Compte compte : comptes) {
                recharges.addAll(compte.getRecharges());
            }

            return recharges;

        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec le id= " + id + " est introuvable.");
        }
    }

    //    ***************************************
//    ***************** API to create Recharge ********************

    @PostMapping(value = "/recharges/create")
    public ResponseEntity<Recharge> createRecharge(@RequestBody RechargeRequest rechargeRequest) {
        Compte compte = compteRepository.findById(rechargeRequest.getNumeroCompte()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Les données invalides.")
        );

        verifyCompteStatus(compte);

        if (!compte.getCodeSecret().equals(rechargeRequest.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

        if (compte.getSolde() < rechargeRequest.getMontant())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Votre solde est insuffisant pour effectuer cette opération.");

        compte.setSolde(compte.getSolde() - rechargeRequest.getMontant());
        compte.setDernierOperation(new Date());

        Recharge recharge = rechargeRepository.save(Recharge.builder()
            .compte(compte)
            .montant(rechargeRequest.getMontant())
            .operateur(rechargeRequest.getOperateur())
            .numeroTelephone(rechargeRequest.getNumeroTelephone())
            .build()
        );

        return new ResponseEntity<>(recharge, HttpStatus.CREATED);
    }

    //    ***************************************
//    ***************** API to create Virement ********************

    @PostMapping(value = "/virements/create")
    public ResponseEntity<Virement> createVirement(@RequestBody VirementRequest virementRequest) {
        Compte compte = compteRepository.findById(virementRequest.getNumeroCompte()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Les données invalides.")
        );
        Compte compteDest = compteRepository.findById(virementRequest.getNumeroCompteDest()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Les données invalides.")
        );

        verifyCompteStatus(compte);

        if (!compte.getCodeSecret().equals(virementRequest.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

        if (compteDest == compte)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Impossible d'effectuer le virement au meme compte.");

        if (compte.getSolde() < virementRequest.getMontant())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Votre solde est insuffisant pour effectuer cette opération.");


//            Le solde va etre decrementé lors qu'il confirme le virement.
//            compte.setSolde(compte.getSolde() - virementRequest.getMontant());
//            compteDest.setSolde(compteDest.getSolde() + virementRequest.getMontant());

        compte.setDernierOperation(new Date());

        Virement virement = virementRepository.save(Virement.builder()
            .compte(compte)
            .destCompte(compteDest)
            .montant(virementRequest.getMontant())
            .notes(virementRequest.getNotes())
            .build()
        );

        mailService.sendVirementCodeMail(compte.getClient().getUser(), virement);

        Notification notification = Notification.builder()
            .client(compte.getClient())
            .contenu("Un <b>nouveau virement</b> à été effectué ! Veuillez verifier votre e-mail pour le confirmer.")
            .build();

        notificationRepository.save(notification);

        return new ResponseEntity<>(virement, HttpStatus.CREATED);
    }

    //    ****** API Client Virements *******

    @GetMapping(value = "/{id}/virements")// return listes des virements  d'un client
    public Collection<Virement> getAllVirements(@PathVariable(value = "id") long id) {
        try {
            Collection<Compte> comptes = getClientComptes(id);
            Collection<Virement> virements = new ArrayList<>();

            for (Compte compte : comptes) {
                virements.addAll(compte.getVirements());
            }

            return virements;

        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec le id= " + id + " est introuvable.");
        }
    }

    //    ************************

    //    ****** API  Virement confirmation *******

    @PostMapping(value = "/virements/{id}/confirm")
    public ResponseEntity<String> virementConfirmation(@PathVariable(value = "id") Long id, @RequestBody HashMap<String, Integer> request) {
        Virement virement = virementRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le virement avec le id= " + id + " est introuvable.")
        );
        // verifier si le virement est confirmé
        if (virement.getStatut().name().equals(VirementStatus.CONFIRMED.name()) || virement.getStatut().name().equals(VirementStatus.RECEIVED.name())) {
            logger.info("Virement status : {}", virement.getStatut().name());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ce virement est déjà confirmé.");
        }

        // verifier le code de verification
        int codeVerification = Math.toIntExact(request.get("codeVerification"));
        if (codeVerification != virement.getCodeVerification())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est invalide.");


        Compte compte = virement.getCompte();
        Compte destCompte = virement.getDestCompte();

        // verifier le solde dispo
        if (compte.getSolde() < virement.getMontant())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Votre solde est insuffisant pour effectuer cette opération.");

        // echange de montant
        compte.setSolde(compte.getSolde() - virement.getMontant());
        destCompte.setSolde(destCompte.getSolde() + virement.getMontant());

        virement.setStatut(VirementStatus.CONFIRMED);
        virementRepository.save(virement);


        return new ResponseEntity<>("Votre virement a été bien confirmé !", HttpStatus.OK);
    }

    //***************************
    //******** API Verify compte number **************
    @PostMapping(value = "/verify_number")
    public ResponseEntity<String> verifyCompteNumber(@RequestBody CompteCredentialsRequest request) {
        Compte compte = compteRepository.findById(request.getNumeroCompte()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le numero est incorrect.")
        );
        verifyCompteStatus(compte);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    //**********************
    //    ****** API  Block Compte *******
    @PutMapping(value = "/comptes/block")
    public ResponseEntity<String> compteBlock(@RequestBody CompteCredentialsRequest request) {
        try {
            Compte compte = compteRepository.findById(request.getNumeroCompte()).get();
            if (compte.getStatut().name().equals(CompteStatus.BLOCKED.name()))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Le compte est déjà bloqué.");
            if (compte.getCodeSecret() != request.getCodeSecret())
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

            compte.setStatut(CompteStatus.PENDING_BLOCKED);
            compteRepository.save(compte);

            return new ResponseEntity<>("Votre demande de blocage a été envoyée aux agents.", HttpStatus.OK);

        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Les données invalides.");
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les données invalides.");
        }
    }
    //***************************
//    ****** API  Suspend Compte *******

    @PutMapping(value = "/comptes/suspend")
    public ResponseEntity<String> compteSuspend(@RequestBody CompteCredentialsRequest request) {
        try {
            Compte compte = compteRepository.findById(request.getNumeroCompte()).get();
            if (compte.getStatut().name().equals(CompteStatus.BLOCKED))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Le compte est bloqué.");
            if (compte.getCodeSecret() != request.getCodeSecret())
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

            compte.setStatut(CompteStatus.PENDING_SUSPENDED);
            compteRepository.save(compte);

            return new ResponseEntity<>("Votre demande de supsension a été envoyée aux agents.", HttpStatus.OK);

        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Les données invalides.");
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les données invalides.");
        }
    }

    @DeleteMapping("/virements/{id}/delete")
    public ResponseEntity<String> deleteVirement(@PathVariable("id") Long id) {
        try {
            Virement virement = virementRepository.findById(id).get();
            logger.info("Virement status : {}", virement.getStatut().name());
            virementRepository.delete(virement);
            return new ResponseEntity<>("Votre virement a été bien supprimé !", HttpStatus.OK);

        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le virement avec le id= " + id + " est introuvable.");
        }
    }

    @GetMapping(path = "{id}/notifications")
    public Collection<Notification> getAllNotifications(@PathVariable("id") Long id) {
        try {
            return clientRepository.findById(id).get().getNotifications();
        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client est introuvable.");
        }
    }

    // api to subscribe to notifications event stream via SSE
    @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Notification> receive() {
        return Flux.create(sink -> notificationService.subscribe(sink::next));
    }

    // this api is only to test notifications "Try sending a cute message to SARA <3 "
    @PostMapping(path = "/notification")
    public String sendNotification(@RequestBody Notification notification) {
        logger.info("NOTIF = {}", notification.getContenu());

        // we set notification reciever to current client.
        notification.setClient(authService.getCurrentUser().getClient());
        notificationService.publish(notification);
        return "OK";
    }

    @PutMapping("{id}/notifications/mark_seen")
    public ResponseEntity<String> markNotificationsSeen(@PathVariable("id") Long id) {
        try {
            Collection<Notification> notifications = clientRepository.findById(id).get().getNotifications()
                .stream()
                .map(notification -> {
                    notification.setLue(true);
                    return notification;
                })
                .collect(Collectors.toList());

            notificationRepository.saveAll(notifications);

            return ResponseEntity.ok().body("OK");

        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client est introuvable.");
        }
    }

    // helper function to check if Compte is active.
    public void verifyCompteStatus(Compte compte) {
        if (!compte.getStatut().name().equals(CompteStatus.ACTIVE.name()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ce compte n'est pas actif.");
    }

    //***************************
//    ****** API to upload avatar image *******

    @PostMapping("/{id}/avatar/upload")
    public ResponseEntity<String> uploadAvatar(@PathVariable("id") Long id, @RequestParam("image") MultipartFile image) {
        try {
            Client client = clientRepository.findById(id).get();

            String fileName = uploadService.store(image);

            // generate download link
            String imageDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/client/api/")
                .path(id + "/avatar")
                .toUriString();

            // check if client has already uploaded an image
            if (client.getPhoto() != null)
                uploadService.delete(client.getPhoto());

            client.setPhoto(fileName);
            clientRepository.save(client);


            return new ResponseEntity<>("Image enregistrée avec succès : " + imageDownloadUri, HttpStatus.CREATED);
        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client est introuvable.");
        }
    }

    //***************************
//    ****** API to get avatar image *******
    @GetMapping("/{id}/avatar")
    public ResponseEntity<Resource> getAvatar(@PathVariable("id") Long id, HttpServletRequest request) {
        try {
            Client client = clientRepository.findById(id).get();

            if (client.getPhoto() == null)
                throw new ResponseStatusException(HttpStatus.OK, "Pas de photo définie.");

            Resource resource = uploadService.get(client.getPhoto());

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

        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client est introuvable.");
        }
    }

    //***************************
//    ****** API to get avatar image *******
    @DeleteMapping("/{id}/avatar/delete")
    public ResponseEntity<String> deleteAvatar(@PathVariable("id") Long id) {
        try {
            Client client = clientRepository.findById(id).get();

            // check if client has already uploaded an image
            if (client.getPhoto() != null)
                uploadService.delete(client.getPhoto());
            else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

            client.setPhoto(null);
            clientRepository.save(client);

            return ResponseEntity.ok("Le fichier est supprimé avec succès.");
        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client est introuvable.");
        }
    }
}
