package com.akinobank.app;

//import com.akinobank.app.models.*;
//import com.akinobank.app.repositories.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

//import java.util.Date;
//import java.util.UUID;

@SpringBootApplication
public class AppApplication  {

//    @Autowired
//    private AdminRepository adminRepository;
//
//    @Autowired
//    private AgentRepository agentRepository;
//
//    @Autowired
//    private AgenceRepository agenceRepository;
//
//    @Autowired
//    private ClientRepository clientRepository;
//
//    @Autowired
//    private CompteRepository compteRepository;


    public static void main(String[] args) {

        ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);
    }

    //Just for test

//    Agence agence =
//    @Override
//    public void run(String... args) throws Exception {
//
//        Admin admin = adminRepository.save(new Admin("nouhaila","nouh@gmai.com","nouhaila","nouhaila"));
//        Agence agence = agenceRepository.save(new Agence(new Date(),new Date(),"Akino","Marrakech",admin));
//        Agent agent = agentRepository.save(new Agent("khalil","kha","kha@gmail.com","password",admin,agence));
//        Client client = clientRepository.save(new Client("client1","prenomclient","client@email.com","clientpasswod",agent));
//        Compte compte1 = compteRepository.save(new Compte(new UUID(6,10),1500,"khalil","Active",new Date(),new Date(),"codeS1",client,null,null));
//        Compte compte2 = compteRepository.save(new Compte(new UUID(6,10),1500,"nouhaila","Active",new Date(),new Date(),"codeS2",client,null,null));
//        Compte compte3 = compteRepository.save(new Compte(new UUID(6,10),1500,"oussama","Active",new Date(),new Date(),"codeS3",client,null,null));
//    }
}
