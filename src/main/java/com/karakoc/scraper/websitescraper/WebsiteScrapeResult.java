package com.karakoc.scraper.websitescraper;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class WebsiteScrapeResult {

    @Id
     private  String id;

    private String startUrl;

    // Kaç sayfa tarandı (pageCount)
    private int pageCount;

    // Taramanın toplam süresi (milisaniye)
    private long timeSpentMs;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> visitedUrls = new ArrayList<>();
    // Bulunan e-posta adresleri
    @ElementCollection
    private Set<String> foundEmails = new HashSet<>();




}

