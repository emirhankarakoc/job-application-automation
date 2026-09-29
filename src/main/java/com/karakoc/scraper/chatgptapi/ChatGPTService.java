package com.karakoc.scraper.chatgptapi;


import com.karakoc.scraper.linkedinjobpostingscraper.LinkedInJobPostingResult;
import com.karakoc.scraper.user.User;

public interface ChatGPTService {
    /**
     * Verilen iş ilanı açıklaması (HTML etiketleri içerebilir) ve CV özetine göre,
     * profesyonel bir iş başvurusu e-postası oluşturur.
     * Yanıt, "subject" (e-posta başlığı) ve "body" (e-posta içeriği) alanlarını içeren JSON formatında döner.
     * @param jobPostingResult is ilaninin ta kendisi.
     * @param user      user nesnesi
     * @return JSON string: {"subject": "mailin basligi", "body": "mailin icerigi, uzun bir metin"}
     */
   ChatGptResponse generateJobApplicationEmail(LinkedInJobPostingResult jobPostingResult, User user);
}
