package com.karakoc.scraper.exceptions.general;

import com.karakoc.scraper.exceptions.RestException;
import com.karakoc.scraper.exceptions.config.AppConfig;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class BadRequestException extends RestException {
    @Getter
    private final HttpStatus httpStatus;

    // Tek parametreli constructor
    public BadRequestException(String msg) {
        super(msg);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    // İki parametreli constructor
    public BadRequestException(String prodMessage, String devMessage) {
        super("Initializing message...");

        // Aktif profili AppConfig'ten alıyoruz
        String activeProfile = AppConfig.getActiveProfile();

        // Aktif profile göre mesajı seçiyoruz
        String finalMessage = "dev".equals(activeProfile) ? devMessage : prodMessage;

        // Mesajı ayarlıyoruz
        this.setMessage(finalMessage);

        this.httpStatus = HttpStatus.BAD_REQUEST;
    }


}
