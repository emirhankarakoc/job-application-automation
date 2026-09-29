package com.karakoc.scraper.orderrequests;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRequestsRepository extends JpaRepository<OrderRequest,String> {
    List<OrderRequest> findAllByStatus(OrderStatus status);
    Optional<OrderRequest> findByLinkedinUrl(String linkedinUrl);
    List<OrderRequest> findAllByUserId(String userId);
}
