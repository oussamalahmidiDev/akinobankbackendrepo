package com.akinobank.app.controllers.agent;

import com.akinobank.app.models.Client;
import com.akinobank.app.repositories.AgenceRepository;
import com.akinobank.app.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;

@Controller
//@RequestMapping("/agent")
public class AgentPanelController {

    @Autowired
    private AgenceRepository agenceRepository;


    @Autowired
    private ClientRepository clientRepository;

    @GetMapping(value = "/all-clients")
    public Collection<Client> getClients(){
        return clientRepository.findAll();
    }
}
