package com.karakoc.scraper.security;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "emirhan karakoc ",
                        email = "emirhankarakoc@yahoo.com",
                        url = "https://youtube.com/@emirwildrift"
                ),
                description = "swagger documentation",
                title = "job posting automation project",
                version = "1.2"
        ),
//        servers = {
//                @Server(url = "https://scrape.emirhankarakoc.com")
//,
//                @Server(url = "http://scrape.emirhankarakoc.com")
//        },

        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}