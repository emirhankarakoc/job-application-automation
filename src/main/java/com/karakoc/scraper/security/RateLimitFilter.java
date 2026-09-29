//package com.karakoc.sofra.security;
//
//import io.github.bucket4j.Bandwidth;
//import io.github.bucket4j.Bucket;
//import io.github.bucket4j.Bucket4j;
//import io.github.bucket4j.Refill;
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.time.Duration;
//
//@Component
//public class RateLimitFilter implements Filter {
//
//    private final Bucket bucket;
//
//    public RateLimitFilter() {
//        // Dakikada 20 isteğe izin veriyoruz
//        Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
//        this.bucket = Bucket4j.builder().addLimit(limit).build();
//    }
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
//            throws IOException, ServletException {
//        // ServletResponse'u HttpServletResponse'e dönüştür
//        if (bucket.tryConsume(1)) {
//            chain.doFilter(servletRequest, servletResponse);
//        } else {
//            if (servletResponse instanceof HttpServletResponse) {
//                HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
//                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//                httpResponse.getWriter().write("Too many requests, please try again later.");
//            }
//        }
//    }
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        // Gerek yok
//    }
//
//    @Override
//    public void destroy() {
//        // Gerek yok
//    }
//}