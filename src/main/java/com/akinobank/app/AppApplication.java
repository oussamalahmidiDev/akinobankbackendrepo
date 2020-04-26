package com.akinobank.app;

import com.akinobank.app.config.Storage;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.utilities.VerificationTokenGenerator;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.persistence.GeneratedValue;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;


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

        // Mocking up currently authenticated user
        Optional<Client> user = clientRepository.findById(1L);

        authService.setCurrentUser(user.orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le client de test est introuvable")
        ).getUser());



    }
}
