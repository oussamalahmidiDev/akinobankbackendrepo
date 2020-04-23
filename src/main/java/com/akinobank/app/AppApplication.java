package com.akinobank.app;

import com.akinobank.app.config.Storage;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import com.akinobank.app.utilities.VerificationTokenGenerator;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

import javax.persistence.GeneratedValue;
import java.util.Date;
import java.util.UUID;


@SpringBootApplication
@EnableConfigurationProperties({ Storage.class })
public class AppApplication  extends SpringBootServletInitializer implements CommandLineRunner   {

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


    public static void main(String[] args)  {

        ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);
    }

//    //Just for test
//    // Add below your test if you want
    @Override
    public void run(String... args) throws Exception {
        //
//        //client
//        User user3 = userRepository.save(User.builder().email("c@ousam.c").role("CLIENT").nom("Oussam").prenom("LAH").build());
//        Client client1 = clientRepository.save(new Client(user3,agentRepository.getOne((long) 3),agenceRepository.getOne((long) 1)));

//        //comptes
//        Compte compte = new Compte();
//        compte.setClient(clientRepository.getOne((long)5));
//        compte.setIntitule("R LAH");
//        compteRepository.save(compte);
//       Compte  compte = compteRepository.save(Compte.builder().client(clientRepository.getOne((long)5)).intitule("MR DAOUL").build());
    }
}
