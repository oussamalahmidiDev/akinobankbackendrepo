package com.akinobank.app.controllers.agent;

import com.akinobank.app.models.Agence;
import com.akinobank.app.repositories.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/agent/api/agence")
@Transactional
@CrossOrigin(value = "*")
public class AgentAgenceController {

    Logger logger = LoggerFactory.getLogger(AgentAgenceController.class);


    @Autowired
    private AgentRepository agentRepository;

    @GetMapping()
    public Agence getAgence() {
        Long id = 1L; //just a test , we will use the token to get agent id
        return agentRepository.findById(id).get().getAgence();
    }
}
