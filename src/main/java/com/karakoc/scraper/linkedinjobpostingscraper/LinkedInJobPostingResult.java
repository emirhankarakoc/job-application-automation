package com.karakoc.scraper.linkedinjobpostingscraper;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class LinkedInJobPostingResult {
    @Id
    private String id;
    private String jobUrl;
    private String roleTitle;
    private String companyProfileUrl;
    @Column(columnDefinition = "TEXT")
    private String roleDescription;
    private long timeSpentMs;

}
