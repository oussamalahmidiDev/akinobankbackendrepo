package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Activity;
import com.akinobank.app.repositories.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        return repository.findAllByUser(profileController.getAgent().getUser());
    }

    @GetMapping("/clients")
    List<Activity> getClientsActivites() {
        return repository.findAllByUserRole(Role.CLIENT);
    }

}
