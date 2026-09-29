package com.karakoc.scraper.linkedinjobpostingscraper;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/linkedin-scrape")
@AllArgsConstructor
public class LinkedInJobPostingController {

     private final LinkedInJobPostingService linkedInJobPostingService;

    @GetMapping("/job")
    public LinkedInJobPostingResult scrapeJob(@RequestParam String jobUrl) throws IOException {
        return linkedInJobPostingService.scrape(jobUrl);
    }
}

