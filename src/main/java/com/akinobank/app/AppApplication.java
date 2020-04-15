package com.akinobank.app;

import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
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


    public static void main(String[] args) {

        ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);
    }

    //Just for test

    @Override
    public void run(String... args) throws Exception {

        //admin
        User user1 = userRepository.save(new User("oussama","vox","oussama@gmail.com","oussama","ADMIN","token1",false));
        Admin admin = adminRepository.save(new Admin(user1));

        //agence
        Agence agence = agenceRepository.save(new Agence("AGENCE de MARRAKECH","MARRAKECH",admin));

        //agent
        User user2 = userRepository.save(new User("khalil","vox","oussama@gmail.com","khalil","Manager","token2",false));
        Agent agent = agentRepository.save(new Agent(user2,admin,agence));

        //client
        User user3 = userRepository.save(new User("abdo","vox","abdo@gmail.com","abdo","client","token3x",false));
        Client client1 = clientRepository.save(new Client(user3,agent,agence));

        //comptes
        Compte compte1 = compteRepository.save(new Compte(1500,"khalil","khalil",new Date(),new Date(),new Date(),"codeS1",client1));
        Compte compte2 = compteRepository.save(new Compte(1500,"nouhaila","Active",new Date(),new Date(),new Date(),"codeS2",client1));
        Compte compte3 = compteRepository.save(new Compte(1500,"oussama","Active",new Date(),new Date(),new Date() ,"codeS3",client1));
    }
}
