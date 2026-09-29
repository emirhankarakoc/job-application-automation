package com.karakoc.scraper.websitescraper;

public interface WebsiteScraperService {
    WebsiteScrapeResult scrape(String startUrl, int maxPages);
}
