package com.akinobank.app;

import com.akinobank.app.config.Storage;
import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.enumerations.RechargeStatus;
import com.akinobank.app.enumerations.Role;
import com.akinobank.app.enumerations.VirementStatus;
import com.akinobank.app.models.*;
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

import java.util.Collection;
import java.util.Date;
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
    private VilleRepository villeRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private VirementRepository virementRepository;

    @Autowired
    private RechargeRepository rechargeRepository;



    @Autowired
    private AuthService authService;




    public static void main(String[] args)  {

        ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);
    }

//    //Just for test
    @Override
    public void run(String... args) throws Exception {

         // Mocking up currently authenticated client
//         Optional<User> authenticatedUser = userRepository.findByEmail("oussama.lahmidi@icloud.com");
//
//         authService.setCurrentUser(authenticatedUser.orElseThrow(
//             () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found")
//         ));
        Ville ville = villeRepository.save(Ville.builder().nom("Marrakech").build());
        Ville ville1 = villeRepository.save(Ville.builder().nom("EL KELAA").build());


        User user1 = userRepository.save(User.builder()
                .nom("DAOULAT")
                .prenom("KHALIL")
                .password("khalil")
                .role(Role.AGENT)
                .email("khalil@gmail.com")
                .numeroTelephone("+21269857452")
                .adresse("ENSA RUE N 1 ")
                .ville("Marrakech")
                .build());
        User user2 = userRepository.save(User.builder()
                .nom("LAHMIDI")
                .prenom("OUSSAMA")
                .role(Role.CLIENT)
                .password("oussama")
                .email("oussama@gmail.com")
                .numeroTelephone("+212623547854")
                .emailConfirmed(true)
                .ville("EL KALAA")
                .adresse("ENSA RUE N 1 ")
                .photo("1.jpg")
                .build());
        User user3 = userRepository.save(User.builder()
                .nom("NIYA")
                .prenom("ABDO")
                .password("abdo")
                .role(Role.CLIENT)
                .email("abdo@gmail.com")
                .numeroTelephone("+212623452154")
                .ville("RABAT")
                .adresse("ENSA RUE N 1 ")
                .photo("2.png")
                .build());
        User user4 = userRepository.save(User.builder()
                .nom("LAH")
                .prenom("INASS")
                .password("inass")
                .role(Role.CLIENT)
                .email("inass@gmail.com")
                .numeroTelephone("+212623547854")
                .ville("RABAT")
                .adresse("ENSA RUE N 1 ")
                .emailConfirmed(true)
                .build());
        User user5 = userRepository.save(User.builder()
                .nom("BOUZITI")
                .prenom("NOUHAILA")
                .password("nouhaila")
                .role(Role.CLIENT)
                .emailConfirmed(true)
                .email("nouhaila@gmail.com")
                .ville("MARRAKECH")
                .adresse("ENSA RUE N 1 ")
                .numeroTelephone("+212624589654")
                .build());


        Admin admin = adminRepository.save(Admin.builder().user(user2).build());

        Agence agence = agenceRepository.save(Agence.builder().libelleAgence("CORONA").ville(ville).admin(admin).build());

        Agent agent = agentRepository.save(Agent.builder().user(user1).agence(agence).build());



        Client client = clientRepository.save(Client.builder().user(user2).agence(agence).build());
        Client client2 = clientRepository.save(Client.builder().user(user3).agence(agence).build());
        Client client3 = clientRepository.save(Client.builder().user(user4).agence(agence).build());
        Client client4 = clientRepository.save(Client.builder().user(user5).agence(agence).build());

        Demande demande = demandeRepository.save(Demande.builder()
                .email("oussama@gmail.com")
                .nom("lahmidi")
                .client(client)
                .prenom("oussama")
                .build());
        Demande demande1 = demandeRepository.save(Demande.builder()
                .email("abdo@gmail.com")
                .nom("niya")
                .client(client)
                .prenom("abdo")
                .build());
        Demande demande2 = demandeRepository.save(Demande.builder()
                .email("nouhaila@gmail.com")
                .nom("bouziti")
                .client(client2)
                .prenom("nouhaila")
                .build());


        Compte compte1 = compteRepository.save(Compte.builder()
                .solde(125000.0)
                .intitule("oussama")
                .client(client)
                .statut(CompteStatus.ACTIVE)
                .build());
        Compte compte2 = compteRepository.save(Compte.builder()
                .solde(516200.0)
                .intitule("khalil")
                .client(client)
                .statut(CompteStatus.BLOCKED)
                .build());
        Compte compte3 = compteRepository.save(Compte.builder()
                .solde(1255840.0)
                .intitule("Abdo")
                .client(client)
                .statut(CompteStatus.ACTIVE)
                .build());

        Virement virement = virementRepository.save(Virement
                .builder()
                .compte(compte1)
                .montant(2000)
                .notes("Take it or leave it")
                .destCompte(compte2)
                .dateDeVirement(new Date())
                .statut(VirementStatus.CONFIRMED)
                .build());

        Recharge recharge = rechargeRepository.save(Recharge
                .builder()
                .compte(compte1)
                .montant(20)
                .statut(RechargeStatus.CONFIRMED)
                .operateur("MAROC TELECOM")
                .numeroTelephone("+21254789654")
                .dateDeRecharge(new Date())
                .build());



    }
}
