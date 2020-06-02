package com.akinobank.app.controllers.client;


import com.akinobank.app.enumerations.CompteStatus;
import com.akinobank.app.enumerations.VirementStatus;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.MailService;
import com.akinobank.app.services.NotificationService;
import com.akinobank.app.services.UploadService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Flux;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/client/api")
@Log4j2
@RequiredArgsConstructor
public class ClientPanelController {

    Logger logger = LoggerFactory.getLogger(ClientPanelController.class);

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private VirementRepository virementRepository;

    @Autowired
    private RechargeRepository rechargeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder encoder;

    private final GoogleAuthenticator gAuth;


    //    ***************** API Client profile ********************

    @GetMapping(value = "/profile") // return Client by id
    public Client getClient() {
        return clientRepository.findByUser(authService.getCurrentUser()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Votre compte est introuvable.")
        );
    }

    @PostMapping(value = "/profile/changer")
    public Client updateProfile(@RequestBody User user) {
        Client client = getClient();

        client.getUser().setEmail(user.getEmail());
        client.getUser().setPrenom(user.getPrenom());
        client.getUser().setNom(user.getNom());
        client.getUser().setNumeroTelephone(user.getNumeroTelephone());
        client.getUser().setAdresse(user.getAdresse());

        return clientRepository.save(client);
    }

    @SneakyThrows
    @GetMapping("/code/generate")
    public void generate(HttpServletResponse response) {

        String email = getClient().getUser().getEmail();
        final GoogleAuthenticatorKey key = gAuth.createCredentials(email);

        //I've decided to generate QRCode on backend site
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        response.setContentType("image/png");
        response.setHeader("X-QR-CODE", getClient().getUser().getSecretKey());

        String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL("Akinobank", email, key);

        BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthURL, BarcodeFormat.QR_CODE, 200, 200);

        ServletOutputStream outputStream = response.getOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);
        outputStream.close();
    }

    @PostMapping("/code/validate")
    public ResponseEntity<String > validateKey(@RequestBody CodeValidationRequest body) {
        User currentClientUser = getClient().getUser();

        if (!gAuth.authorizeUser(currentClientUser.getEmail(), body.getCode()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le code est invalide.");

        currentClientUser.set_2FaEnabled(true);
        userRepository.save(currentClientUser);

        return ResponseEntity.ok("");
    }

    @PostMapping("/change_password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest request) {
        User currentClientUser = getClient().getUser();
        if (!encoder.matches(request.getOldPassword(), currentClientUser.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'ancien mot de passe est incorrect.");
        if (!request.getNewPassword().equals(request.getConfPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Les deux mots de passe ne sont pas identiques.");

        currentClientUser.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(currentClientUser);

        return new ResponseEntity<>(HttpStatus.OK);
    }

//    ***************************************
//    ***************** API Client comptes ********************

    @GetMapping(value = "/comptes")// return listes des comptes d'un client
    public Collection<Compte> getClientComptes() {
        return getClient().getComptes();
    }

    //    ** API to change code secret ***
    @PostMapping("/comptes/management/changer_code")
    public Compte changeCodeSecret(@RequestBody @Valid CodeChangeRequest request) {
        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le compte est introuvable")
        );

        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "L'ancien code est incorrect.");

        if (!request.getNewCodeSecret().equals(request.getNewCodeSecretConf()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Les nouveaux codes ne sont pas identiques.");

        compte.setCodeSecret(request.getNewCodeSecret());
        return compteRepository.save(compte);

    }

//    ***************************************
    //    ***************** API Client Recharges ********************

    @GetMapping(value = "/recharges")// return listes des recharges  d'un client
    public Collection<Recharge> getAllRecharges() {
        Collection<Compte> comptes = getClientComptes();
        Collection<Recharge> recharges = new ArrayList<>();

        for (Compte compte : comptes) {
            recharges.addAll(compte.getRecharges());
        }

        return recharges;
    }

    //    ***************************************
//    ***************** API to create Recharge ********************

    @PostMapping(value = "/recharges/create")
    public ResponseEntity<Recharge> createRecharge(@RequestBody @Valid RechargeRequest rechargeRequest) {
        Compte compte = compteRepository.findByNumeroCompteAndClient(rechargeRequest.getNumeroCompte(), getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );

        verifyCompteStatus(compte);


        if (!compte.getCodeSecret().equals(rechargeRequest.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

        if (compte.getSolde() < rechargeRequest.getMontant())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Votre solde est insuffisant pour effectuer cette opération.");

        compte.setSolde(compte.getSolde() - rechargeRequest.getMontant());
        compte.setDernierOperation(new Date());

        Recharge recharge = rechargeRepository.save(Recharge.builder()
            .compte(compte)
            .montant(rechargeRequest.getMontant())
            .operateur(rechargeRequest.getOperateur())
            .numeroTelephone(rechargeRequest.getNumeroTelephone())
            .build()
        );

        return new ResponseEntity<>(recharge, HttpStatus.CREATED);
    }

    //    ***************************************
//    ***************** API to create Virement ********************

    @PostMapping(value = "/virements/create")
    public ResponseEntity<Virement> createVirement(@RequestBody VirementRequest virementRequest) {
        Compte compte = compteRepository.findByNumeroCompteAndClient(virementRequest.getNumeroCompte(), getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );

        if (!compte.getCodeSecret().equals(virementRequest.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

        verifyCompteStatus(compte);

        Compte compteDest = compteRepository.findById(virementRequest.getNumeroCompteDest()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte de destination est erroné.")
        );

        if (compteDest == compte)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Impossible d'effectuer le virement au meme compte.");

        if (compte.getSolde() < virementRequest.getMontant())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Votre solde est insuffisant pour effectuer cette opération.");


//            Le solde va etre decrementé lors qu'il confirme le virement.
//            compte.setSolde(compte.getSolde() - virementRequest.getMontant());
//            compteDest.setSolde(compteDest.getSolde() + virementRequest.getMontant());

        compte.setDernierOperation(new Date());

        Virement virement = virementRepository.save(Virement.builder()
            .compte(compte)
            .destCompte(compteDest)
            .montant(virementRequest.getMontant())
            .notes(virementRequest.getNotes())
            .build()
        );

        mailService.sendVirementCodeMail(getClient().getUser(), virement);

        // Notification notification = Notification.builder()
        //     .client(compte.getClient())
        //     .contenu("Un <b>nouveau virement</b> à été effectué ! Veuillez verifier votre e-mail pour le confirmer.")
        //     .build();

        // notificationRepository.save(notification);

        return new ResponseEntity<>(virement, HttpStatus.CREATED);
    }

    //    ****** API Client Virements *******

    @GetMapping(value = "/virements")// return listes des virements  d'un client
    public Collection<Virement> getAllVirements() {
        Collection<Compte> comptes = getClientComptes();
        Collection<Virement> virements = new ArrayList<>();

        for (Compte compte : comptes) {
            virements.addAll(compte.getVirements());
        }

        return virements;

    }

    //    ************************

    //    ****** API  Virement confirmation *******

    @PostMapping(value = "/virements/{id}/confirm")
    public ResponseEntity<String> virementConfirmation(@PathVariable(value = "id") Long id, @RequestBody HashMap<String, Integer> request) {
        Virement virement = virementRepository.findByIdAndAndCompte_Client(id, getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le virement avec le id= " + id + " est introuvable.")
        );
        // verifier si le virement est confirmé
        if (virement.getStatut().name().equals(VirementStatus.CONFIRMED.name()) || virement.getStatut().name().equals(VirementStatus.RECEIVED.name())) {
            logger.info("Virement status : {}", virement.getStatut().name());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ce virement est déjà confirmé.");
        }

        // verifier le code de verification
        int codeVerification = Math.toIntExact(request.get("codeVerification"));
        if (codeVerification != virement.getCodeVerification())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est invalide.");


        Compte compte = virement.getCompte();
        Compte destCompte = virement.getDestCompte();

        // verifier le solde dispo
        if (compte.getSolde() < virement.getMontant())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Votre solde est insuffisant pour effectuer cette opération.");

        // echange de montant
        compte.setSolde(compte.getSolde() - virement.getMontant());
        destCompte.setSolde(destCompte.getSolde() + virement.getMontant());

        virement.setStatut(VirementStatus.CONFIRMED);
        virementRepository.save(virement);


        return new ResponseEntity<>("Votre virement a été bien confirmé !", HttpStatus.OK);
    }

    //***************************
    //******** API Verify compte number **************
    @PostMapping(value = "/verify_number")
    public ResponseEntity<String> verifyCompteNumber(@RequestBody CompteCredentialsRequest request) {
        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );
        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

        verifyCompteStatus(compte);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //**********************
    //    ****** API  Block Compte *******
    @PutMapping(value = "/comptes/block")
    public ResponseEntity<Compte> compteBlock(@RequestBody Compte request) {
        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );
        if (compte.getStatut().name().equals(CompteStatus.BLOCKED.name()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Le compte est déjà bloqué.");
        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

        compte.setStatut(CompteStatus.PENDING_BLOCKED);
        compte.setRaison(request.getRaison());

        compteRepository.save(compte);

        return new ResponseEntity<>(compte, HttpStatus.OK);
    }

    //***************************
//    ****** API  Suspend Compte *******

    @PutMapping(value = "/comptes/suspend")
    public ResponseEntity<Compte> compteSuspend(@RequestBody Compte request) {
        Compte compte = compteRepository.findByNumeroCompteAndClient(request.getNumeroCompte(), getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le nº de compte est erroné.")
        );

        if (compte.getStatut().name().equals(CompteStatus.BLOCKED))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Le compte est bloqué.");
        if (!compte.getCodeSecret().equals(request.getCodeSecret()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est incorrect.");

        compte.setStatut(CompteStatus.PENDING_SUSPENDED);
        compte.setRaison(request.getRaison());

        compteRepository.save(compte);

        return new ResponseEntity<>(compte, HttpStatus.OK);
    }

    @DeleteMapping("/virements/{id}/delete")
    public ResponseEntity<String> deleteVirement(@PathVariable("id") Long id) {
        Virement virement = virementRepository.findByIdAndAndCompte_Client(id, getClient()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le virement avec le id= " + id + " est introuvable.")
        );
        logger.info("Virement status : {}", virement.getStatut().name());
        virementRepository.delete(virement);
        return new ResponseEntity<>("Votre virement a été bien supprimé !", HttpStatus.OK);
    }

    @GetMapping(path = "/notifications")
    public Collection<Notification> getAllNotifications() {
        return getClient().getNotifications();
    }

    // api to subscribe to notifications event stream via SSE
    @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Notification> receive() {
        return Flux.create(sink -> notificationService.subscribe(sink::next));
    }

    @PostMapping(path = "/notification")
    public String sendNotification(@RequestBody Notification notification) {
        logger.info("NOTIF = {}", notification.getContenu());

        // we set notification reciever to current client.
        notification.setClient(getClient());
        notificationService.publish(notification);
        return "OK";
    }

    // ******** this api to mark all notifications as seen *******
    @PutMapping("/notifications/mark_seen")
    public ResponseEntity<String> markNotificationsSeen() {
        Collection<Notification> notifications = getClient().getNotifications()
            .stream()
            .map(notification -> {
                notification.setLue(true);
                return notification;
            })
            .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);

        return ResponseEntity.ok().body("OK");
    }

    // helper function to check if Compte is active.
    public void verifyCompteStatus(Compte compte) {
        if (!compte.getStatut().name().equals(CompteStatus.ACTIVE.name()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ce compte n'est pas actif.");
    }

    //***************************
//    ****** API to upload avatar image *******

    @PostMapping("/avatar/upload")
//    @CacheEvict(cacheNames = "clients", allEntries = true)
    public ResponseEntity<?> uploadAvatar(@RequestParam("image") MultipartFile image) {
        Client client = getClient();

        String fileName = uploadService.store(image);

        // generate download link
        String imageDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/client/api/")
            .path("/avatar/")
            .path(fileName)
            .toUriString();

        // check if client has already uploaded an image
        if (client.getUser().getPhoto() != null)
            uploadService.delete(client.getUser().getPhoto());

        client.getUser().setPhoto(fileName);
        clientRepository.save(client);

        Map<String, String> response = new HashMap<>();
        response.put("link", fileName);

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    //***************************
//    ****** API to get avatar image *******
    @GetMapping("/avatar/{filename}")
    public ResponseEntity<Resource> getAvatar(HttpServletRequest request, @PathVariable("filename") String filename) {
        Client client = getClient();
//        System.out.println(filename);

        if (client.getUser().getPhoto() == null)
            throw new ResponseStatusException(HttpStatus.OK, "Pas de photo définie.");

        Resource resource = uploadService.get(client.getUser().getPhoto());

        // setting content-type header
        String contentType = null;
        try {
            // setting content-type header according to file type
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Type indéfini.");
        }
        // setting content-type header to generic octet-stream
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    //***************************
//    ****** API to get avatar image *******
    @DeleteMapping("/avatar/delete")
//    @CacheEvict(cacheNames = "clients", allEntries = true)
    public ResponseEntity<String> deleteAvatar() {
        Client client = getClient();

        // check if client has already uploaded an image
        if (client.getUser().getPhoto() != null)
            uploadService.delete(client.getUser().getPhoto());
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        client.getUser().setPhoto(null);
        clientRepository.save(client);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
