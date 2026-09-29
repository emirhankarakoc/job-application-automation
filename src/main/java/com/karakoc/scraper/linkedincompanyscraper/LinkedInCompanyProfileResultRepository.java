package com.karakoc.scraper.linkedincompanyscraper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkedInCompanyProfileResultRepository extends JpaRepository<LinkedInCompanyProfileResult,String> {
}
