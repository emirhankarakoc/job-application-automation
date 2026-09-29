package com.karakoc.scraper.accounts;

import com.karakoc.scraper.ScraperApplication;
import com.karakoc.scraper.account.requests.LoginRequest;
import com.karakoc.scraper.account.requests.LoginResponse;
import com.karakoc.scraper.account.requests.RegisterRequest;
import com.karakoc.scraper.security.TokenManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;

@SpringBootTest(classes = ScraperApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class GetMeTest {

    final String[] content_type_json = {"Content-Type","application/json"};


    @Test
    void getMe200Test(){
        //register , login and getme.
        var dummy = RegisterRequest.builder()
                .email("getmetest_testemailaddress1"+"@gmail.com")
                .password("testpassword")
                .build();
        given().header(content_type_json[0],content_type_json[1]).body(dummy).when().post("/accounts/register").then().statusCode(200);
        var request = LoginRequest.builder()
                .email(dummy.getEmail())
                .password(dummy.getPassword()).build();
        LoginResponse response = given().header(content_type_json[0],content_type_json[1]).body(request).when().post("/accounts/login").then().statusCode(200)
                .extract()
                .as(LoginResponse.class);
        //accesstoken = response.getAccessToken();
        given()
                .header(content_type_json[0],content_type_json[1])
                .header("Authorization","Bearer " + response.getAccessToken())
        .when()
                .get("/accounts/getme")
        .then()
                .statusCode(200);
    }

    @Test
    void getMe401Test(){
        //without token

        given()
                .header(content_type_json[0],content_type_json[1])
                .when()
                .get("/accounts/getme")
                .then()
                .statusCode(401);
    }
    @Test
    void getMe403Test(){
        //this method needs only jwt token at header, if we send wrong one, we should get 403 forbidden.

        //wrong token

        given()
                .header(content_type_json[0],content_type_json[1])
                .header("Authorization","Bearer " + "wrongToken")
                .when()
                .get("/accounts/getme")
                .then()
                .statusCode(403);
    }
}
