package com.karakoc.scraper.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // İstek bilgilerini alalım
        String method = request.getMethod();
        String endpoint = request.getRequestURI();

        // İstek bilgilerini loglayalım
        log.info("{}: {} , {}", method, endpoint);


        // İstek zincirine devam edelim
        filterChain.doFilter(request, response);
    }
}
