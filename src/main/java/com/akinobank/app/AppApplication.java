package com.akinobank.app;

import com.akinobank.app.config.Storage;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@SpringBootApplication
@EnableConfigurationProperties({ Storage.class })
public class AppApplication implements CommandLineRunner     {

    Logger logger = LoggerFactory.getLogger(AppApplication.class);
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgenceRepository agenceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DemandeRepository demandeRepository;


    @Autowired
    private AuthService authService;




    public static void main(String[] args)  {

        ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);
    }

//    //Just for test
    @Override
    public void run(String... args) throws Exception {

         // Mocking up currently authenticated client
         Optional<User> authenticatedUser = userRepository.findByEmail("oussama.lahmidi@icloud.com");

         authService.setCurrentUser(authenticatedUser.orElseThrow(
             () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found")
         ));



    }
}
