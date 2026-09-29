package com.karakoc.scraper.accounts;


import com.karakoc.scraper.ScraperApplication;
import com.karakoc.scraper.account.AuthController;
import com.karakoc.scraper.account.AuthService;
import com.karakoc.scraper.account.requests.RegisterRequest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
@SpringBootTest(classes = ScraperApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class RegisterTest {

    final String[] content_type_json = {"Content-Type","application/json"};

    @Test
    @Order(1)
    void register200Request(){
        var req = RegisterRequest.builder()
                .email("registertest_testemailaddress"+"@gmail.com")
                .password("testpassword")
                .build();
        given()
                .header(content_type_json[0],content_type_json[1])
                .body(req)
                .when()
                .post("/accounts/register")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(2)

    void register400Request(){
        //trying 2 times with same e-mail address.
        //so we can get "this email address is using"
        var req = RegisterRequest.builder()
                .email("registertest_testemailaddress2"+"@gmail.com")
                .password("testpassword")
                .build();
        given()
                .header(content_type_json[0],content_type_json[1])
                .body(req)
                .when()
                .post("/accounts/register")
                        .then()
                                .statusCode(200);

        given()
                .header(content_type_json[0],content_type_json[1])
                .body(req)
                .when()
                .post("/accounts/register")
                .then()
                .statusCode(400);
    }
}
