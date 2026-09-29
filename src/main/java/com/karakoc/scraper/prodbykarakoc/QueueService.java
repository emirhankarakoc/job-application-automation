package com.karakoc.scraper.prodbykarakoc;

import com.karakoc.scraper.orderrequests.OrderRequest;
import com.karakoc.scraper.sentmails.SentMail;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.Map;

public interface QueueService {
    void run(String linkedinJobPostingUrl) throws IOException, MessagingException;

    OrderRequest createOrder(String userId, String linkedinUrl);

    QueueController.SentMailResponse seeSentMailByOrderId(String id);
    String allOrderRequestsLength();
    //alttaki iki metod , scheduledService'i commente aldigim zaman 0 implementation oluyor ama programin islemesi icin lazim.
    void sendOrderDetailsToUser(OrderRequest request) throws MessagingException;
    void sendOrderFailedToUser(OrderRequest or,String body) throws MessagingException;
}
