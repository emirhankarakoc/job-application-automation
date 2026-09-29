package com.karakoc.scraper.account.requests;

import lombok.*;


@Builder
@AllArgsConstructor
@NoArgsConstructor

@Data
public class LoginResponse {
    private String accessToken;
}
