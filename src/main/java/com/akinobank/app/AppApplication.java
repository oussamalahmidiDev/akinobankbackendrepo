package com.akinobank.app;

import com.akinobank.app.config.Storage;
import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Notification;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.CompteRepository;
import com.akinobank.app.repositories.NotificationRepository;
import com.akinobank.app.repositories.UserRepository;
import com.akinobank.app.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

//import com.akinobank.app.repositories.SessionRepository;


@SpringBootApplication
@EnableConfigurationProperties({Storage.class})
@Controller
//@EnableRed
public class AppApplication implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(AppApplication.class);
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
    @Autowired
    private CompteRepository compteRepository;
    //
    @Autowired
    private UserRepository userRepository;
    //
//    @Autowired
//    private VilleRepository villeRepository;
//
//    @Autowired
//    private DemandeRepository demandeRepository;
//
//    @Autowired
//    private VirementRepository virementRepository;
//
//    @Autowired
//    private RechargeRepository rechargeRepository;
//
    @Autowired
    private PasswordEncoder encoder;
//
//    @Autowired
//    private SessionRepository sessionRepository;
//
//    @Autowired
//    private SessionsUtils sessionsUtils;
//

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MailService mail;
//    @GetMapping("/redis")
//    @ResponseBody
//    public Session testRedis() {
//        return sessionRepository.save(
//            Session.builder()
//                .user(userRepository.getOne(27L))
//                .build()
//        );
//    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);
    }

    @GetMapping("/test/notify")
    @ResponseStatus(HttpStatus.OK)
    public void send(@RequestBody Notification notification) {
        template.convertAndSendToUser("oussama.lahmidi@icloud.com", "/topic/notifications", notification);
    }

//    @GetMapping("/test/notify")
//    @ResponseBody
//    public String getNotification(@RequestBody Notification notification) {
//        List<User> receivers = new ArrayList<>();
//        receivers.add(userRepository.findByEmail("oussama.lahmidi@icloud.com"));
//        notification.setReceiver(receivers);
//
////        notification.setClient(userRepository.findByEmail("oussama.lahmidi@icloud.com").getClient());
//
//        notificationRepository.save(notification);
//        // Increment Notification by one
//
//        // Push notifications to front-end
////        template.convertAndSend("/topic/notifications", notification);
////        template.convertAndSend();
////        template.convertAndSendToUser("oussama.lahmidi@icloud.com", "/topic/notifications", notification);
//        return "Notifications successfully sent !";
//    }

//    @ResponseStatus(HttpStatus.OK)
//    @PostMapping("/test/num")
//    @ResponseBody
//    public Compte testNumeroCompte(@RequestBody @Valid CompteCredentialsRequest request) {
//        logger.info("Valid");
//        return compteRepository.findById(request.getNumeroCompte()).orElseThrow(
//            () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
//        );
//    }

    @GetMapping("/test/email")
    @ResponseBody
    public String sendcompte() {

        mail.sendCompteDetails(userRepository.findByEmail("oussama.lahmidi@icloud.com"), compteRepository.getOne("2276784734691951"));
        return "OK";
    }

//    @GetMapping("/test")
//    public void runTests() {
//        Ville ville = villeRepository.save(Ville.builder().nom("Marrakech").build());
//        Ville ville1 = villeRepository.save(Ville.builder().nom("EL KELAA").build());
//
//
//        User user1 = userRepository.save(User.builder()
//            .nom("DAOULAT")
//            .prenom("KHALIL")
//            .password(encoder.encode("khalil"))
//            .email("daoulat.khalil@gmail.com")
//            .role(Role.AGENT)
//            .numeroTelephone("+21269857452")
//            .adresse("ENSA RUE N 1 ")
//            .ville("Marrakech")
//            .build());
//        User user2 = userRepository.save(User.builder()
//            .nom("LAHMIDI")
//            .prenom("OUSSAMA")
//            .role(Role.ADMIN)
//            .password(encoder.encode("khalil"))
//            .email("oussama@gmail.com")
//            .numeroTelephone("+212623547854")
//            .emailConfirmed(true)
//            .ville("EL KALAA")
//            .adresse("ENSA RUE N 1 ")
//            .photo("1.jpg")
//            .build());
//        User user3 = userRepository.save(User.builder()
//            .nom("NIYA")
//            .prenom("ABDO")
//            .password("abdo")
//            .role(Role.CLIENT)
//            .email("abdo@gmail.com")
//            .numeroTelephone("+212623452154")
//            .ville("RABAT")
//            .adresse("ENSA RUE N 1 ")
//            .photo("2.png")
//            .build());
//        User user4 = userRepository.save(User.builder()
//            .nom("LAH")
//            .prenom("INASS")
//            .password("inass")
//            .role(Role.CLIENT)
//            .email("inass@gmail.com")
//            .numeroTelephone("+212623547854")
//            .ville("RABAT")
//            .adresse("ENSA RUE N 1 ")
//            .emailConfirmed(true)
//            .build());
//        User user5 = userRepository.save(User.builder()
//            .nom("BOUZITI")
//            .prenom("NOUHAILA")
//            .password("nouhaila")
//            .role(Role.CLIENT)
//            .emailConfirmed(true)
//            .email("nouhaila@gmail.com")
//            .ville("MARRAKECH")
//            .adresse("ENSA RUE N 1 ")
//            .numeroTelephone("+212624589654")
//            .build());
//
//
//        Admin admin = adminRepository.save(Admin.builder().user(user2).build());
//
//        Agence agence = agenceRepository.save(Agence.builder().libelleAgence("CORONA").ville(ville).admin(admin).build());
//
//        Agent agent = agentRepository.save(Agent.builder().user(user1).agence(agence).build());
//
//
//        Client client = clientRepository.save(Client.builder().user(user2).agence(agence).build());
//        Client client2 = clientRepository.save(Client.builder().user(user3).agence(agence).build());
//        Client client3 = clientRepository.save(Client.builder().user(user4).agence(agence).build());
//        Client client4 = clientRepository.save(Client.builder().user(user5).agence(agence).build());
//
//        Demande demande = demandeRepository.save(Demande.builder()
//            .email("oussama@gmail.com")
//            .nom("lahmidi")
//            .client(client)
//            .prenom("oussama")
//            .build());
//        Demande demande1 = demandeRepository.save(Demande.builder()
//            .email("abdo@gmail.com")
//            .nom("niya")
//            .client(client)
//            .prenom("abdo")
//            .build());
//        Demande demande2 = demandeRepository.save(Demande.builder()
//            .email("nouhaila@gmail.com")
//            .nom("bouziti")
//            .client(client2)
//            .prenom("nouhaila")
//            .build());
//
//
//        Compte compte1 = compteRepository.save(Compte.builder()
//            .solde(125000.0)
//            .intitule("oussama")
//            .client(client)
//            .statut(CompteStatus.ACTIVE)
//            .build());
//        Compte compte2 = compteRepository.save(Compte.builder()
//            .solde(516200.0)
//            .intitule("khalil")
//            .client(client)
//            .statut(CompteStatus.BLOCKED)
//            .build());
//        Compte compte3 = compteRepository.save(Compte.builder()
//            .solde(1255840.0)
//            .intitule("Abdo")
//            .client(client)
//            .statut(CompteStatus.ACTIVE)
//            .build());
//
//        Virement virement = virementRepository.save(Virement
//            .builder()
//            .compte(compte1)
//            .montant(2000)
//            .notes("Take it or leave it")
//            .destCompte(compte2)
//            .dateDeVirement(new Date())
//            .statut(VirementStatus.CONFIRMED)
//            .build());
//
//        Recharge recharge = rechargeRepository.save(Recharge
//            .builder()
//            .compte(compte1)
//            .montant(20)
//            .statut(RechargeStatus.CONFIRMED)
//            .operateur("MAROC TELECOM")
//            .numeroTelephone("+21254789654")
//            .dateDeRecharge(new Date())
//            .build());
//
//
//    }

    //    //Just for test
    @Override
    public void run(String... args) {
//        Create a default ADMIN USER ACCOUNT

        User user = User.builder()
            .email("admin@akino.com")
            .nom("Admin")
            .prenom("Akino")
            .role(Role.ADMIN)
            .id(1L)
            .password(encoder.encode("password"))
            .build();
        if (!userRepository.findById(1L).isPresent())
            userRepository.save(user);


    }
}
