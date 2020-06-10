package com.akinobank.app.controllers.client;

import com.akinobank.app.models.Activity;
import com.akinobank.app.repositories.ActivityRepository;
import com.akinobank.app.services.AuthService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/client/api/activities")
@Log4j2
public class ActivitiesController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ActivityRepository repository;

    @GetMapping("")
    List<Activity> getActivites(
        @RequestParam(value = "offset", defaultValue = "0", required = false) int offset,
        @RequestParam(value = "limit", defaultValue = "3", required = false) int limit
    ) {
        return repository.findAllByUser(authService.getCurrentUser(),
            PageRequest.of(offset, limit, Sort.by("timestamp").descending()));
    }

}
