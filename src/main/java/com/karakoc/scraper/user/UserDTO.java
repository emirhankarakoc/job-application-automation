package com.karakoc.scraper.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {
    private String id;
    private String email;
    private String role;
    private String cvSummary;
    private String cvUrlKey;
    private LocalDateTime cvFileUpdatedAt;
    private LocalDateTime cvSummaryUpdatedAt;
    private String cvFileName;
    private String fullname;
    private String phoneNumber;
    private String emailForMailSending;
    private String address;
    private String githubUrl;
    private String linkedinUrl;
    private String website;

}
