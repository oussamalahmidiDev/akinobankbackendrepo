package com.akinobank.app.controllers.client;

import com.akinobank.app.enumerations.ActivityCategory;
import com.akinobank.app.models.Client;
import com.akinobank.app.models.CodeValidationRequest;
import com.akinobank.app.models.PasswordChangeRequest;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.ClientRepository;
import com.akinobank.app.repositories.UserRepository;
import com.akinobank.app.services.ActivitiesService;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.UploadService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/client/api/profile")
@Log4j2
public class ClientProfileController {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder encoder;


    @Autowired
    private SecretGenerator secretGenerator;

    @Autowired
    private QrDataFactory qrDataFactory;


    @Autowired
    private CodeVerifier verifier;

    @Autowired
    private ActivitiesService activitiesService;


    @GetMapping() // return Client by id
    public Client getClient() {
        return clientRepository.findByUser(authService.getCurrentUser()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Votre compte est introuvable.")
        );
    }

    @PostMapping(value = "/changer")
    public Client updateProfile(@RequestBody User user) {
        Client client = getClient();

        client.getUser().setEmail(user.getEmail());
        client.getUser().setPrenom(user.getPrenom());
        client.getUser().setNom(user.getNom());
        client.getUser().setNumeroTelephone(user.getNumeroTelephone());
        client.getUser().setAdresse(user.getAdresse());
        clientRepository.save(client);

        activitiesService.save("Changement des informations personnelles", ActivityCategory.PROFILE_U);
        return client;
    }

    @SneakyThrows
    @GetMapping("/code/generate")
    public void generate(HttpServletResponse response, HttpServletRequest request) {

        String email = getClient().getUser().getEmail();

        String secret = secretGenerator.generate();
        log.info("QR secret key generated : {}", secret);
        String data = qrDataFactory.newBuilder()
            .label(email)
            .secret(secret)
            .issuer("Akinobank")
            .build().getUri();

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);

        response.setContentType("image/png");
        response.setHeader("X-QR-CODE", secret);


        ServletOutputStream outputStream = response.getOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);
        outputStream.close();
    }

    @PostMapping("/code/validate")
    public ResponseEntity<String > validateKey(@RequestBody CodeValidationRequest body, HttpServletRequest request) {
        User currentClientUser = getClient().getUser();
        final String secretKey = request.getHeader("X-QR-CODE");
        log.info("QR secret key validation : {}, code : {}", secretKey, body.getCode());

        if (!verifier.isValidCode(secretKey, body.getCode()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le code est invalide.");

        currentClientUser.set_2FaEnabled(true);
        currentClientUser.setSecretKey(secretKey);

        userRepository.save(currentClientUser);

        return ResponseEntity.ok("");
    }

    @PostMapping("/change_password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changePassword(@RequestBody PasswordChangeRequest request) {
        User currentClientUser = getClient().getUser();
        if (!encoder.matches(request.getOldPassword(), currentClientUser.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'ancien mot de passe est incorrect.");
        if (!request.getNewPassword().equals(request.getConfPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Les deux mots de passe ne sont pas identiques.");

        currentClientUser.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(currentClientUser);

        activitiesService.save("Changement de mot de passe", ActivityCategory.PROFILE_PASS_CHANGE);

    }

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

        activitiesService.save("Changement de photo de profil", ActivityCategory.PROFILE_U);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

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

        activitiesService.save("Suppression de photo de profil", ActivityCategory.PROFILE_U);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
