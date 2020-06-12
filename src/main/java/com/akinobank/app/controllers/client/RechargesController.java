package com.akinobank.app.controllers.client;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.models.Client;
import com.akinobank.app.models.Compte;
import com.akinobank.app.models.Recharge;
import com.akinobank.app.models.RechargeRequest;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.repositories.RechargeRepository;
import com.akinobank.app.services.ActivitiesService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@RestController
@RequestMapping("/client/api/recharges")
@Log4j2
public class RechargesController {

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private RechargeRepository rechargeRepository;

    @Autowired
    private ComptesController comptesController;

    @Autowired
    private ClientPanelController profileController;

    @Autowired
    private ActivitiesService activitiesService;

    @GetMapping()// return listes des recharges  d'un client
    public Collection<Recharge> getAllRecharges() {
        Collection<Compte> comptes = comptesController.getClientComptes();
        Collection<Recharge> recharges = new ArrayList<>();

        for (Compte compte : comptes) {
            recharges.addAll(compte.getRecharges());
        }

        return recharges;
    }

    @PostMapping(value = "/create")
    public ResponseEntity<Recharge> createRecharge(@RequestBody @Valid RechargeRequest rechargeRequest) {
        Client client = profileController.getClient();

        Compte compte = compteRepository.findByNumeroCompteAndClient(rechargeRequest.getNumeroCompte(), client).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );

        comptesController.verifyCompteStatus(compte, false);


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

        activitiesService.save(
            String.format("Effectue d'une recharge télephonique à partir du compte nº %s d'un montant de %.2f envoyée au numéro %s", compte.getNumeroCompte(), recharge.getMontant(), recharge.getNumeroTelephone()),
            ActivityCategory.RECHARGES_C
        );

        return new ResponseEntity<>(recharge, HttpStatus.CREATED);
    }


}
