package com.karakoc.scraper.linkedincompanyscraper;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class LinkedInCompanyProfileResult {

    @Id
    private String id;
    private String companyProfileUrl;
    private String website;    // Şirketin web sitesi
    private long timeSpentMs;  // Ne kadar sürede çekildi

 }
