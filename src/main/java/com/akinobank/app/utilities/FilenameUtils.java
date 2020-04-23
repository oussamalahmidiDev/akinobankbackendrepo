package com.akinobank.app.utilities;

import org.springframework.web.multipart.MultipartFile;

public class FilenameUtils {

    // helper function to get extension from file name
    public static String getExtension (MultipartFile file) {
        return file.getOriginalFilename().split("\\.")[1];
    }
}
