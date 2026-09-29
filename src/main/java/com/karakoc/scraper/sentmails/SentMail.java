package com.karakoc.scraper.sentmails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentMail {
    @Id
    private String id;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;
    private String jobPostingScraperResultId;
    private String companyScraperResultId;
    private String websiteScraperResultId;
    private String chatGptResponseId;

    private String orderId;

    // Gönderim zamanını tutar.
    private LocalDateTime sentAt;

}
