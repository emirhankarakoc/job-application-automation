package com.karakoc.scraper.websitescraper;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scraper")
@AllArgsConstructor
public class WebsiteScraperController {

     private final WebsiteScraperService websiteScraperService;

    @GetMapping("/scrape")
    public WebsiteScrapeResult scrapeUrl(@RequestParam String url, @RequestParam int maxPage) {
        return websiteScraperService.scrape(url,maxPage);
    }
}

