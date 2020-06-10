package com.akinobank.app.controllers.client;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.VirementStatus;
import com.akinobank.app.models.Client;
import com.akinobank.app.models.Compte;
import com.akinobank.app.models.Virement;
import com.akinobank.app.models.VirementRequest;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.repositories.VirementRepository;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.MailService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/client/api/virements")
@Log4j2
public class VirementsController {

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private VirementRepository virementRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private ClientProfileController profileController;

    @Autowired
    private ComptesController comptesController;

    @Autowired
    private ActivitiesService activitiesService;



    @GetMapping()// return listes des virements  d'un client
    public Collection<Virement> getAllVirements() {
        Collection<Compte> comptes = comptesController.getClientComptes();
        Collection<Virement> virements = new ArrayList<>();

        for (Compte compte : comptes) {
            virements.addAll(compte.getVirements());
        }

        return virements;

    }

    @PostMapping(value = "/create")
    public ResponseEntity<Virement> createVirement(@RequestBody VirementRequest virementRequest) {
        Client client = profileController.getClient();
        Compte compte = compteRepository.findByNumeroCompteAndClient(virementRequest.getNumeroCompte(), client).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );

        if (!compte.getCodeSecret().equals(virementRequest.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

        comptesController.verifyCompteStatus(compte);

        Compte compteDest = compteRepository.findById(virementRequest.getNumeroCompteDest()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte de destination est erroné.")
        );

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

        mailService.sendVirementCodeMail(client.getUser(), virement);

        activitiesService.save(
            String.format("Envoie d'un virement de compte d'expedition %s au compte de nº %s d'un montant de %.2f", compte.getNumeroCompte(), compteDest.getNumeroCompte(), virementRequest.getMontant()),
            ActivityCategory.VIREMENTS_C
        );

        // Notification notification = Notification.builder()
        //     .client(compte.getClient())
        //     .contenu("Un <b>nouveau virement</b> à été effectué ! Veuillez verifier votre e-mail pour le confirmer.")
        //     .build();

        // notificationRepository.save(notification);

        return new ResponseEntity<>(virement, HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/confirm")
    public ResponseEntity<String> virementConfirmation(@PathVariable(value = "id") Long id, @RequestBody HashMap<String, Integer> request) {
        Virement virement = virementRepository.findByIdAndAndCompte_Client(id, profileController.getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le virement avec le id= " + id + " est introuvable.")
        );
        // verifier si le virement est confirmé
        if (virement.getStatut().name().equals(VirementStatus.CONFIRMED.name()) || virement.getStatut().name().equals(VirementStatus.RECEIVED.name())) {
            log.info("Virement status : {}", virement.getStatut().name());
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

        activitiesService.save(
            String.format("Confirmation du virement nº %d", virement.getId()),
            ActivityCategory.VIREMENTS_CONF
        );

        return new ResponseEntity<>("Votre virement a été bien confirmé !", HttpStatus.OK);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteVirement(@PathVariable("id") Long id) {
        Client client = profileController.getClient();

        Virement virement = virementRepository.findByIdAndAndCompte_Client(id, client).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le virement avec le id= " + id + " est introuvable.")
        );
        log.info("Virement status : {}", virement.getStatut().name());
        virementRepository.delete(virement);

        activitiesService.save(
            String.format("Suppression du virement nº %d", virement.getId()),
            ActivityCategory.VIREMENTS_D
        );

        return new ResponseEntity<>("Votre virement a été bien supprimé !", HttpStatus.OK);
    }
}