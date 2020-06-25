package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Activity;
import com.akinobank.app.models.Agence;
import com.akinobank.app.models.Agent;
import com.akinobank.app.repositories.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/agent/api/activities")
public class AgentActivitiesController {

    @Autowired
    private ActivityRepository repository;

    @Autowired
    private AgentProfileController profileController;

    @GetMapping("")
    List<Activity> getActivites() {
        Agent agent = profileController.getAgent();
        Agence agence = agent.getAgence();
        return repository.findAllByUserAndUser_Agent_Agence(
                agent.getUser(),
                agence,
                PageRequest.of(0, 200, Sort.by("timestamp").descending()));
    }

    @GetMapping("/clients")
    List<Activity> getClientsActivites() {
        return repository.findAllByUserRoleAndUser_Agent_Agence(
                Role.CLIENT,
                profileController.getAgent().getAgence(),
                PageRequest.of(0, 200, Sort.by("timestamp").descending()));
    }

}
