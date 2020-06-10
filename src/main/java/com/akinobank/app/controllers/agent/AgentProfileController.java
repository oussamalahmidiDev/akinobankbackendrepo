package com.akinobank.app.controllers.agent;

import com.akinobank.app.models.Agent;
import com.akinobank.app.models.PasswordChangeRequest;
import com.akinobank.app.models.User;
import com.akinobank.app.repositories.*;
import com.akinobank.app.services.AuthService;
import com.akinobank.app.services.MailService;
import com.akinobank.app.services.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/agent/api/profile")
@Transactional
@CrossOrigin(value = "*")
public class AgentProfileController {

    Logger logger = LoggerFactory.getLogger(AgentProfileController.class);

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgenceRepository agenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private MailService mailService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder encoder;

    @GetMapping()
    public Agent getAgent() {
        return agentRepository.findByUser(authService.getCurrentUser()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "L'agent est introuvable.")
        );
    }

    @PostMapping("/avatar/upload")
    @CacheEvict(cacheNames = "clients", allEntries = true)
    public ResponseEntity<?> uploadAvatar(@RequestParam("image") MultipartFile image) {
        System.out.println(image);
        Agent agent = getAgent();

        String fileName = uploadService.store(image);

        // generate download link
        String imageDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/client/api/")
            .path("/avatar/")
            .path(fileName)
            .toUriString();

        // check if client has already uploaded an image
        if (agent.getUser().getPhoto() != null)
            uploadService.delete(agent.getUser().getPhoto());

        agent.getUser().setPhoto(fileName);
        agentRepository.save(agent);

        Map<String, String> response = new HashMap<>();
        response.put("link", imageDownloadUri);

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @PostMapping("/modifier/password")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        System.out.println(passwordChangeRequest);
        Agent currentAgent = getAgent();
        Boolean isMatch = encoder.matches(passwordChangeRequest.getOldPassword(), currentAgent.getUser().getPassword());

        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Votre mot de passe ne correspond pas.");
        }

        if (!isMatch) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mauvais mot de passe.");
        }

        currentAgent.getUser().setPassword(encoder.encode(passwordChangeRequest.getNewPassword()));
        agentRepository.save(currentAgent);
    }

    @PostMapping("/modifier/user")
    public User modifyAgentParam(@RequestBody User user) {
        System.out.println(user);
        User oldUser = getAgent().getUser();

        oldUser.setEmail(user.getEmail());
        oldUser.setNom(user.getNom());
        oldUser.setPrenom(user.getPrenom());
        oldUser.setNumeroTelephone(user.getNumeroTelephone());
        userRepository.save(oldUser);
        return oldUser;
    }

    @GetMapping("/avatar/{filename}")
    public ResponseEntity<Resource> getAvatar(HttpServletRequest request, @PathVariable("filename") String filename) {
        Agent agent = getAgent();
        System.out.println(filename);

//        if (agent.getUser().getPhoto() == null )
//            throw new ResponseStatusException(HttpStatus.OK, "Pas de photo définie.");

        Resource resource = uploadService.get(agent.getUser().getPhoto());

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


    @DeleteMapping("/avatar/delete/{filename}")
    public ResponseEntity deleteAgentPhoto(@PathVariable("filename") String filename) {
        uploadService.delete(filename);
        getAgent().getUser().setPhoto(null);
        agentRepository.save(getAgent());
        return new ResponseEntity(HttpStatus.OK);
    }


}
