package com.karakoc.scraper.orderrequests;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class OrderRequest {
    @Id
    private String id;
    private String linkedinUrl;
    private LocalDateTime localDateTime;
    private String sentMailId;
    private OrderStatus status;
    private String userId;
}
