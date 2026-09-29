package com.karakoc.scraper.linkedinjobpostingscraper;


import java.io.IOException;

public interface LinkedInJobPostingService {
    LinkedInJobPostingResult scrape(String jobUrl) throws IOException;
}
