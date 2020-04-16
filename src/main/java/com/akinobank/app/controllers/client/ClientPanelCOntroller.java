package com.akinobank.app.controllers.client;


import com.akinobank.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/client")
public class ClientPanelCOntroller {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private VirementnRepository virementnRepository;

    @Autowired
    private RechargeRepository rechargeRepository;

    @Autowired
    private NotificationsRepository notificationsRepository;
}
