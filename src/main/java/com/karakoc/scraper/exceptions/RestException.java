package com.karakoc.scraper.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = false)
public class RestException extends RuntimeException {

    private HttpStatus httpStatus;
    private String message;

    public RestException(String message) {
        super(message);
        this.message = message;
    }

    public RestException(String message, HttpStatus httpStatus) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
    }

    // Mesajı sonradan ayarlamak için setter
    public void setMessage(String message) {
        this.message = message;
    }
}
