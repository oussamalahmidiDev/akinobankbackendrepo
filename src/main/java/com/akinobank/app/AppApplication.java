package com.akinobank.app;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.UUID;


@SpringBootApplication
public class AppApplication implements CommandLineRunner {

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

//    private static final Logger logger = LoggerFactory.getLogger(AppApplication.class); // for logs



    public static void main(String[] args) {

        ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);

//            System.out.println("Current Directory = " + System.getProperty("user.dir")); // for logs
//            SpringApplication.run(AppApplication.class, args);
//            logger.info("just a test info log");
    }

    //Just for test
    // Add below your test if you want
    @Override
    public void run(String... args) throws Exception {

        //admin
        User user1 = userRepository.save(new User("testt","vox","test@gmail.com",Role.ADMIN));
        Admin admin = adminRepository.save(new Admin(user1));

        //agence
        Agence agence = agenceRepository.save(new Agence("MARRAKECH","AGENCE de MARRAKECH",admin));
        Agence agence1 = agenceRepository.save(new Agence("FES","AGENCE de FES",admin));

        //agent
        User user2 = userRepository.save(new User("khalil","vox","ffsd@gmail.com",Role.AGENT));
        User user4 = userRepository.save(new User("inas","vox","inas@gmail.com",Role.AGENT));
        Agent agent = agentRepository.save(new Agent(user2,admin,agence));
        Agent agent1 = agentRepository.save(new Agent(user4,admin,agence1));

        //client
        User user3 = userRepository.save(new User("nouhaila","test","nouhail@gmail.com",Role.CLIENT));
        User user5 = userRepository.save(new User("inass","test","inass@gmail.com",Role.CLIENT));
        Client client1 = clientRepository.save(new Client(user3,agent,agence));
        Client client2 = clientRepository.save(new Client(user5,agent,agence));

        //comptes
        Compte compte1 = compteRepository.save(new Compte("khalil",client1,0));
        Compte compte2 = compteRepository.save(new Compte("nouhaila",client1,0));
        Compte compte3 = compteRepository.save(new Compte("oussama",client1,0));
    }
}
