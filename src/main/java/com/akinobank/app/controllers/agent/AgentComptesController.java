package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.models.Compte;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.services.ActivitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("/agent/api/comptes")
@RestController
public class AgentComptesController {

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ActivitiesService activitiesService;

    @GetMapping("{id}")
    public Compte getCompteById(@PathVariable String id) {
        return compteRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le compte avec nº " + id + " est introuvable.")
        );
    }

    @DeleteMapping("{id}/supprimer")
    @ResponseStatus(HttpStatus.OK)
    public void deleteCompte(@PathVariable String id) {
        compteRepository.delete(getCompteById(id));

        activitiesService.save(
            String.format("Suppression définitive du compte nº %s", id),
            ActivityCategory.COMPTES_D
        );
    }

    @PutMapping("{id}/modifier")
    public Compte updateCompte(@PathVariable String id, @RequestBody Compte body) {
        Compte compteToUpdate = getCompteById(id);
        compteToUpdate.setSolde(body.getSolde());
        compteToUpdate.setIntitule(body.getIntitule());

        compteRepository.save(compteToUpdate);

        activitiesService.save(
            String.format("Modification des informations du compte nº %s", compteToUpdate.getNumeroCompte()),
            ActivityCategory.COMPTES_U
        );

        return compteToUpdate;
    }

    @PutMapping("{id}/modifier/status")
    public Compte updateCompteStatus(@PathVariable String id, @RequestParam(value = "status") String status) {
        Compte compteToUpdate = getCompteById(id);
        compteToUpdate.setStatut(CompteStatus.valueOf(status));
        compteRepository.save(compteToUpdate);

        activitiesService.save(
            String.format("Modification de statut du compte nº %s. Le nouveau statut : %s", compteToUpdate.getNumeroCompte(), compteToUpdate.getStatut().name()),
            ActivityCategory.COMPTES_U
        );
        return compteToUpdate;
    }

}
