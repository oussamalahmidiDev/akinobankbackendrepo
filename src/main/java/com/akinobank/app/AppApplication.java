package com.akinobank.app;

//import com.akinobank.app.models.*;
//import com.akinobank.app.repositories.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.UUID;

//import java.util.Date;
//import java.util.UUID;

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

        User user1 = userRepository.save(new User("oussama","vox","oussama@gmail.com","oussama","ADMIN"));
        Admin admin = adminRepository.save(new Admin(user1));

        User user2 = userRepository.save(new User("khalil","vox","oussama@gmail.com","khalil","Manager"));
        Agent agent = agentRepository.save(new Agent(user2));

        User user3 = userRepository.save(new User("abdo","vox","abdo@gmail.com","abdo","client"));
        Client client = clientRepository.save(new Client(user3));


        Compte compte1 = compteRepository.save(new Compte(new UUID(6,10),1500,"khalil","khalil",new Date(),new Date(),new Date(),"codeS1",client,null,null));
        Compte compte2 = compteRepository.save(new Compte(new UUID(6,10),1500,"nouhaila","Active",new Date(),new Date(),new Date(),"codeS2",client,null,null));
        Compte compte3 = compteRepository.save(new Compte(new UUID(6,10),1500,"oussama","Active",new Date(),new Date(),new Date() ,"codeS3",client,null,null));
    }
}
