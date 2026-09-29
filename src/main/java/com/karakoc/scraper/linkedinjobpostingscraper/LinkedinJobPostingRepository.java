package com.karakoc.scraper.linkedinjobpostingscraper;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkedinJobPostingRepository extends JpaRepository<LinkedInJobPostingResult,String> {
}
