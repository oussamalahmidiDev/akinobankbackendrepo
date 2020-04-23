package com.akinobank.app.services;

import com.akinobank.app.config.Storage;
import com.akinobank.app.exceptions.FileNotFoundException;
import com.akinobank.app.exceptions.StorageException;
import com.akinobank.app.utilities.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class UploadService {

    Logger logger = LoggerFactory.getLogger(UploadService.class);

    // default allowed extensions (images)
    private final String[] ALLOWED_EXTENSIONS = {
        "jpeg",
        "jpg",
        "png",
    };

    // default upload folder
    private final Path fileStorageLocation;

    public UploadService (Storage fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
            .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            throw new StorageException( "Erreur de création de dossier de fichiers.", e);
        }
    }

    public String store (MultipartFile file) {

        // check file extension
        if (!isExtensionAllowed(FilenameUtils.getExtension(file)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce fichier n'est pas autorisé.");

        // normaliser le nom de fichier avec un nom standard (timestamp)
        String fileName = StringUtils.cleanPath(System.currentTimeMillis() + "." + FilenameUtils.getExtension(file));

        logger.info("New file name : {}", fileName);

        try {
            //verifier le nom de fichier
            if (fileName.contains(".."))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom de fichier est invalide : " + fileName);

            // enregistrement de fichier (remplacer le fichier s'il existe deja)
            Path target = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de stocker le fichier " + fileName);
        }
    }

    public Resource get (String fileName) {
        try {
            // recuperer le path de fichier demandé
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists())
                return resource;
            else
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable : " + fileName);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable : " + fileName);
        }
    }

    public void delete (String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists())
                resource.getFile().delete();
            else
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable : " + fileName);

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable : " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isExtensionAllowed (String extension) {
        for (String currentExtension: ALLOWED_EXTENSIONS) {
            if (currentExtension.equalsIgnoreCase(extension))
                return true;
        }
        return false;
    }
}
