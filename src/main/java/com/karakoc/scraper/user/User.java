package com.karakoc.scraper.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class User {
    @Id
    private String id;
    private String email;
    @JsonIgnore
    private String password;
    private String role;


    @Column(columnDefinition = "TEXT")
    private String cvSummary;
    private LocalDateTime cvSummaryUpdatedAt;
    private String cvUrlKey;
    private String cvFileName;
    private LocalDateTime cvFileUpdatedAt;


    private String fullname;
    private String phoneNumber;
    private String emailForMailSending;
    private String address;
    private String githubUrl;
    private String linkedinUrl;
    private String website;


    public static UserDTO userToDTO(User user) {
        var dto = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .cvSummary(user.getCvSummary())
                .cvUrlKey(user.getCvUrlKey())
                .cvFileUpdatedAt(user.getCvFileUpdatedAt())
                .cvSummaryUpdatedAt(user.getCvSummaryUpdatedAt())
                .cvFileName(user.getCvFileName())
                .fullname(user.getFullname())
                .phoneNumber(user.getPhoneNumber())
                .emailForMailSending(user.getEmailForMailSending())
                .address(user.getAddress())
                .githubUrl(user.getGithubUrl())
                .linkedinUrl(user.getLinkedinUrl())
                .website(user.getWebsite())
                .build();
        return dto;
    }

    public static List<UserDTO> usersToDTOS(List<User> userlist) {
        List<UserDTO> response = new ArrayList<>();
        for (User user : userlist) {
            response.add(userToDTO(user));
        }
        return response;
    }
}
