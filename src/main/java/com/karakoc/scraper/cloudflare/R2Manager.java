package com.karakoc.scraper.cloudflare;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class R2Manager implements R2Service {
    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url}")
    private String publicEndpointUrl;

    public R2Manager(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Uploads a file and returns the generated key and public URL.
     */
    @Override
    public String uploadFile(MultipartFile file) {
        try {
            File convertedFile = convertMultiPartToFile(file);
            String key = uploadToR2(convertedFile);
            Files.deleteIfExists(convertedFile.toPath());
            return key;

        } catch (IOException e) {
            return "Failed to upload document: "+  e.getMessage().toString();
        }
    }

    /**
     * Uploads a file to Cloudflare R2 and returns the unique key.
     */
    private String uploadToR2(File file) {
        String key = UUID.randomUUID().toString();
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .build(),
                file.toPath()
        );
        return key;
    }

    /**
     * Generates a public URL from the given key.
     */
    private String generatePublicUrl(String key) {
        return String.format("%s/%s", publicEndpointUrl, key);
    }

    /**
     * Returns the public URL for a stored key.
     */
    public String getPublicUrl(String key) {
        return generatePublicUrl(key);
    }

    /**
     * Converts a MultipartFile to a File.
     */
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        }
        return convertedFile;
    }

    public void destroy(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
            System.out.println("Deleted object: " + key);
        } catch (Exception e) {
            System.err.println("Failed to delete object: " + key);
            e.printStackTrace();
        }
    }


}


