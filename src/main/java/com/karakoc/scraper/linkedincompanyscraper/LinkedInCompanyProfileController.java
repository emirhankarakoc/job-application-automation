package com.karakoc.scraper.linkedincompanyscraper;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/linkedin-scrape")
@AllArgsConstructor
public class LinkedInCompanyProfileController {

    private final LinkedInCompanyProfileService companyProfileService;

    /**
     * Örnek istek:
     * GET /api/linkedin/company?companyProfileUrl=https://www.linkedin.com/company/ptrglobal
     */
    @GetMapping("/company")
    public LinkedInCompanyProfileResult scrapeCompany(@RequestParam String companyProfileUrl) {
        return companyProfileService.scrapeCompanyProfile(companyProfileUrl);
    }
}
