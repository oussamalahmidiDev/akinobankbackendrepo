package com.akinobank.app.controllers.client;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.models.Client;
import com.akinobank.app.models.CodeChangeRequest;
import com.akinobank.app.models.Compte;
import com.akinobank.app.models.CompteCredentialsRequest;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.services.ActivitiesService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Collection;


@RestController
@RequestMapping("/client/api/comptes")
@Log4j2
public class ComptesController {
    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ClientProfileController profileController;

    @Autowired
    private ActivitiesService activitiesService;


    @GetMapping()
    public Collection<Compte> getClientComptes() {
        return profileController.getClient().getComptes();
    }

    //    ** API to change code secret ***
    @PostMapping("/changer_code")
    public Compte changeCodeSecret(@RequestBody @Valid CodeChangeRequest request) {
        Client client = profileController.getClient();
        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), client).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le compte est introuvable")
        );

        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "L'ancien code est incorrect.");

        if (!request.getNewCodeSecret().equals(request.getNewCodeSecretConf()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Les nouveaux codes ne sont pas identiques.");

        compte.setCodeSecret(request.getNewCodeSecret());
        compteRepository.save(compte);

        activitiesService.save(
            String.format("Changement du code secret de compte nº %s", compte.getNumeroCompte()),
            ActivityCategory.COMPTES_CODE_CHANGE
        );
        return compte;
    }

    @PostMapping(value = "/verify_number")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void verifyCompteNumber(@RequestBody CompteCredentialsRequest request) {
        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), profileController.getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );
        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le code est incorrect.");
        verifyCompteStatus(compte);
    }

    @PutMapping(value = "/block")
    public ResponseEntity<Compte> compteBlock(@RequestBody Compte request) {
        Client client = profileController.getClient();

        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), client).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );
        if (compte.getStatut().name().equals(CompteStatus.BLOCKED.name()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le compte est déjà bloqué.");
        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le code est incorrect.");

        compte.setStatut(CompteStatus.PENDING_BLOCKED);
        compte.setRaison(request.getRaison());

        compteRepository.save(compte);

        activitiesService.save(
            String.format("Envoie d'une demande de blocage du compte nº %s pour la raison : %s", compte.getNumeroCompte(), request.getRaison()),
            ActivityCategory.COMPTES_DEMANDE_BLOCK
        );

        return new ResponseEntity<>(compte, HttpStatus.OK);
    }

    @PutMapping(value = "/suspend")
    public ResponseEntity<Compte> compteSuspend(@RequestBody Compte request) {
        Client client = profileController.getClient();

        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), client).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );

        if (compte.getStatut().name().equals(CompteStatus.BLOCKED))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le compte est bloqué.");
        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le code est incorrect.");

        compte.setStatut(CompteStatus.PENDING_SUSPENDED);
        compte.setRaison(request.getRaison());

        compteRepository.save(compte);

        activitiesService.save(
            String.format("Envoie d'une demande de suspension du compte nº %s pour la raison : %s", compte.getNumeroCompte(), request.getRaison()),
            ActivityCategory.COMPTES_DEMANDE_SUSPEND
        );

        return new ResponseEntity<>(compte, HttpStatus.OK);
    }

    // helper function to check if Compte is active.
    public void verifyCompteStatus(Compte compte) {
        if (!compte.getStatut().name().equals(CompteStatus.ACTIVE.name()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce compte n'est pas actif.");
    }


}
