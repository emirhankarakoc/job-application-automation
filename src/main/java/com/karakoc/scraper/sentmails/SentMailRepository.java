package com.karakoc.scraper.sentmails;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SentMailRepository extends JpaRepository<SentMail,String> {
    Optional<SentMail> findSentMailByOrderId(String orderId);
}
