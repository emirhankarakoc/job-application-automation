package com.karakoc.scraper.mailservice;

import com.karakoc.scraper.orderrequests.OrderRequest;
import jakarta.mail.MessagingException;

import java.util.List;
import java.util.Map;

public interface MailService {

      Map<String, Object> sendMultipartfileMailToCompanyMails(OrderRequest orderRequest, List<String> toList, String body, String subject);
     void sendBasicMail(String to,String body) throws MessagingException;

}
