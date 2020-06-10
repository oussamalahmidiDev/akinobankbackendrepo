package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.models.Compte;
import com.akinobank.app.repositories.CompteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("/agent/api/comptes")
@RestController
public class AgentComptesController {

    @Autowired
    private CompteRepository compteRepository;

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
    }

    @PutMapping("{id}/modifier")
    public Compte updateCompte(@PathVariable String id, @RequestBody Compte body) {
        Compte compteToUpdate = getCompteById(id);
        compteToUpdate.setSolde(body.getSolde());
        compteToUpdate.setIntitule(body.getIntitule());

        return compteRepository.save(compteToUpdate);
    }

    @PutMapping("{id}/modifier/status")
    public Compte updateCompteStatus(@PathVariable String id, @RequestParam(value = "status") String status) {
        Compte compteToUpdate = getCompteById(id);
        compteToUpdate.setStatut(CompteStatus.valueOf(status));

        return compteRepository.save(compteToUpdate);
    }

}
