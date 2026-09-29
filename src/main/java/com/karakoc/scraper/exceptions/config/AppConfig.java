package com.karakoc.scraper.exceptions.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppConfig {

    private static Environment environment;

    @Autowired
    public void setEnvironment(Environment env) {
        AppConfig.environment = env;
    }

    public static String getActiveProfile() {
        return environment.getProperty("emirhan-karakoc.profil");
    }
}
