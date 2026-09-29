package com.karakoc.scraper.exceptions.general;

import com.karakoc.scraper.exceptions.RestException;
import com.karakoc.scraper.exceptions.config.AppConfig;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class UnauthorizatedException extends RestException {
    @Getter
    private final HttpStatus httpStatus;

    // Tek parametreli constructor
    public UnauthorizatedException(String msg) {
        super(msg);
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }

    // İki parametreli constructor
    public UnauthorizatedException(String prodMessage, String devMessage) {
        super("Initializing message...");

        // Aktif profili AppConfig'ten alıyoruz
        String activeProfile = AppConfig.getActiveProfile();

        // Aktif profile göre mesajı seçiyoruz
        String finalMessage = "dev".equals(activeProfile) ? devMessage : prodMessage;

        // Mesajı ayarlıyoruz
        this.setMessage(finalMessage);

        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }
}
