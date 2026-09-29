package com.karakoc.scraper.cloudflare;

import org.springframework.web.multipart.MultipartFile;

public interface R2Service {
    /**
     * Uploads a file and returns the public URL.
     */
    String uploadFile(MultipartFile file);

    /**
     * Generates and returns the public URL for a given key.
     */
    String getPublicUrl(String key);

    void destroy(String key);
}
