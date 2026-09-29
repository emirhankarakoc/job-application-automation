package com.karakoc.scraper.accounts;

import com.karakoc.scraper.ScraperApplication;
import com.karakoc.scraper.account.requests.LoginRequest;
import com.karakoc.scraper.account.requests.RegisterRequest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;

@SpringBootTest(classes = ScraperApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class LoginTest {

    final String[] content_type_json = {"Content-Type","application/json"};

    @Test
    @Order(1)
    void login200Test(){
        var dummy = RegisterRequest.builder()
                .email("logintest_testemailaddress"+"@gmail.com")
                .password("testpassword")
                .build();
        given().header(content_type_json[0],content_type_json[1]).body(dummy).when().post("/accounts/register").then().statusCode(200);
        var request = LoginRequest.builder()
                .email(dummy.getEmail())
                .password(dummy.getPassword()).build();
        given()
                .header(content_type_json[0],content_type_json[1])
                .body(request)
        .when()
                .post("/accounts/login")
        .then()
                .statusCode(200);
    }
    @Test
    @Order(2)
    void login403Test(){
        var dummy = RegisterRequest.builder()
                .email("logintest_testemailaddress1"+"@gmail.com")
                .password("testpassword1")
                .build();
        given().header(content_type_json[0],content_type_json[1]).body(dummy).when().post("/accounts/register").then().statusCode(200);
        var request = LoginRequest.builder()
                .email(dummy.getEmail() + "wrong")
                .password(dummy.getPassword() + "wrong").build();
        given()
                .header(content_type_json[0],content_type_json[1])
                .body(request)
                .when()
                .post("/accounts/login")
                .then()
                .statusCode(403);
    }

}
