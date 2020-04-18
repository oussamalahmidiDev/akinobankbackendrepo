package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Agence;
import com.akinobank.app.models.Agent;
import com.akinobank.app.models.Client;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.AgenceRepository;
import com.akinobank.app.repositories.AgentRepository;
import com.akinobank.app.repositories.ClientRepository;
import com.akinobank.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent")
public class AgentPanelController {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgenceRepository agenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping(value = "/clients")
    public List<User> getClients(){
        return userRepository.findAllByRole(Role.CLIENT);
    }

    @PostMapping("/client/ajouter")
    public Client addClient(@RequestBody  User user,Long id_agent,Long id_agence ){
        user.setRole(Role.CLIENT);
        Agent agent = agentRepository.findById(id_agent).get();
        Agence agence = agenceRepository.findById(id_agence).get();
        userRepository.save(user);
        Client client = new Client(user,agent ,agence);
        return clientRepository.save(client);
    }

    @DeleteMapping(value = "/client/supprimer")
    public String deleteClient(@PathVariable("id") Long id){
        userRepository.deleteById(id);
        return "YOUR CLIENT HAS DELETED";
    }
}
