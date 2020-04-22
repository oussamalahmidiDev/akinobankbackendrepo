package com.akinobank.app.controllers.client;


import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/client/api")
public class ClientPanelController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private VirementRepository virementRepository;

    @Autowired
    private RechargeRepository rechargeRepository;

    @Autowired
    private NotificationsRepository notificationsRepository;

    @Autowired
    private MailService mailService;


    //    ***************** API Client profile ********************

    @GetMapping(value = "/profile/{id}") // return Client by id
    public User getClient(@PathVariable(value = "id") Long id) {
        try {
            return clientRepository.findById(id).get().getUser();
        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "le client avec id = " + id + " est introuvable.");
        }
    }

//    ***************************************
//    ***************** API Client comptes ********************

    @GetMapping(value = "/{id}/comptes")// return listes des comptes d'un client
    public Collection<Compte> getClientComptes(@PathVariable(value = "id")Long id) {
        try {
            return compteRepository.findAllByClientId(id);
        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec id = " + id + " est introuvable.");
        }
    }

//    ***************************************
    //    ***************** API Client Recharges ********************

    @GetMapping(value = "/{id}/recharges")// return listes des recharges  d'un client
    public Collection<Recharge> getAllRecharges(@PathVariable(value = "id") long id) {
        try {
            Collection<Compte> comptes = getClientComptes(id);
            Collection<Recharge> recharges = new ArrayList<>();

            for (Compte compte: comptes) {
                recharges.addAll(compte.getRecharges());
            }

            return  recharges;

        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client avec le id= " + id + " est introuvable.");
        }
    }

    //    ***************************************
//    ***************** API to create Recharge ********************

    @PostMapping(value = "/recharges/create")
    public ResponseEntity<Recharge> createRecharge (@RequestBody RechargeRequest rechargeRequest) {
        try {
            Compte compte = compteRepository.findById(rechargeRequest.getNumeroCompte()).get();

            if (compte.getCodeSecret() != rechargeRequest.getCodeSecret())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le code est incorrect.");

            if (compte.getSolde() < rechargeRequest.getMontant())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Votre solde est insuffisant pour effectuer cette opération.");

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

        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les données invalides.");
        }
    }

    //    ***************************************
//    ***************** API to create Virement ********************

    @PostMapping(value = "/virements/create")
    public ResponseEntity<Virement> createVirement (@RequestBody VirementRequest virementRequest) {
        try {
            Compte compte = compteRepository.findById(virementRequest.getNumeroCompte()).get();
            Compte compteDest = compteRepository.findById(virementRequest.getNumeroCompteDest()).get();

            if (compte.getCodeSecret() != virementRequest.getCodeSecret())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le code est incorrect.");

            if (compteDest == compte)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'effectuer le virement au meme compte.");

            if (compte.getSolde() < virementRequest.getMontant())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Votre solde est insuffisant pour effectuer cette opération.");


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

            return new ResponseEntity<>(virement, HttpStatus.CREATED);

        } catch (NoSuchElementException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les données invalides.");
        }
    }

}
