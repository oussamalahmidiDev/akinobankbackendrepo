package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Activity;
import com.akinobank.app.models.Agence;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.Client;
import com.akinobank.app.repositories.ActivityRepository;
import com.akinobank.app.repositories.AgenceRepository;
import com.akinobank.app.repositories.ClientRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/agent/api/activities")
@Log4j2
public class AgentActivitiesController {

    @Autowired
    private ActivityRepository repository;

    @Autowired
    private AgentProfileController profileController;

    @Autowired
    private AgenceRepository agenceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping("")
    List<Activity> getActivites() {
        Agent agent = profileController.getAgent();
        Agence agence = agent.getAgence();
        return repository.findAllByUser(
                agent.getUser(),
                PageRequest.of(0, 20, Sort.by("timestamp").descending()));
    }

    @GetMapping("/clients")
    List<Activity> getClientsActivites() {
        long id = profileController.getAgent().getAgence().getId();
        Agence agence = agenceRepository.findById(id).get();
        return repository.findAllByUserRoleAndUser_Client_Agence(
                Role.CLIENT, agence, PageRequest.of(0, 20, Sort.by("timestamp").descending()));
    }


}
