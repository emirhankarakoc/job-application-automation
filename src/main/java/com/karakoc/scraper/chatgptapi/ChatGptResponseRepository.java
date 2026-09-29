package com.karakoc.scraper.chatgptapi;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatGptResponseRepository extends JpaRepository<ChatGptResponse, String> {
    // İhtiyaç duyarsanız ek sorgu metodları burada tanımlanabilir.
}
