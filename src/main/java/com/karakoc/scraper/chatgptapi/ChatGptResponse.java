package com.karakoc.scraper.chatgptapi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptResponse {

    @Id
    private String id; // UUID gibi benzersiz bir id

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;
}
