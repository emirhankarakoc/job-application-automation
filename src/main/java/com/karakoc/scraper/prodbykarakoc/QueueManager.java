package com.karakoc.scraper.prodbykarakoc;


import com.karakoc.scraper.chatgptapi.ChatGPTService;
import com.karakoc.scraper.chatgptapi.ChatGptResponse;
import com.karakoc.scraper.chatgptapi.ChatGptResponseRepository;
import com.karakoc.scraper.exceptions.general.BadRequestException;
import com.karakoc.scraper.exceptions.general.NotfoundException;
import com.karakoc.scraper.linkedincompanyscraper.LinkedInCompanyProfileResult;
import com.karakoc.scraper.linkedincompanyscraper.LinkedInCompanyProfileResultRepository;
import com.karakoc.scraper.linkedincompanyscraper.LinkedInCompanyProfileService;
import com.karakoc.scraper.linkedinjobpostingscraper.LinkedInJobPostingResult;
import com.karakoc.scraper.linkedinjobpostingscraper.LinkedInJobPostingService;
import com.karakoc.scraper.linkedinjobpostingscraper.LinkedinJobPostingRepository;
import com.karakoc.scraper.mailservice.MailService;
import com.karakoc.scraper.orderrequests.OrderRequest;
import com.karakoc.scraper.orderrequests.OrderRequestsRepository;
import com.karakoc.scraper.orderrequests.OrderStatus;
import com.karakoc.scraper.sentmails.SentMail;
import com.karakoc.scraper.sentmails.SentMailRepository;
import com.karakoc.scraper.user.User;
import com.karakoc.scraper.user.UserRepository;
import com.karakoc.scraper.websitescraper.WebsiteScrapeResult;
import com.karakoc.scraper.websitescraper.WebsiteScrapeResultRepository;
import com.karakoc.scraper.websitescraper.WebsiteScraperService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class QueueManager implements QueueService {
    private final LinkedInJobPostingService jobPostingService;
    private final LinkedinJobPostingRepository linkedinJobPostingRepository;
    private final LinkedInCompanyProfileService companyProfileService;
    private final WebsiteScraperService websiteScraperService;
    private final SentMailRepository sentMailRepository;
    private final MailService mailService;
    private final ChatGPTService chatGPTService;
    private final OrderRequestsRepository orderRequestsRepository;
    private final UserRepository userRepository;
    private final LinkedInCompanyProfileResultRepository linkedInCompanyProfileResultRepository;
    private final WebsiteScrapeResultRepository websiteScrapeResultRepository;
    private final ChatGptResponseRepository chatGptResponseRepository;


    @Override
    public OrderRequest createOrder(String userId, String linkedinUrl) {
        if (linkedinUrl == null || linkedinUrl.isEmpty()) {
            throw new BadRequestException("Invalid linkedin URL");
        }
        if (!linkedinUrl.contains("www.linkedin.com/jobs/view")) {
            throw new BadRequestException("Invalid linkedin URL");
        }
        if (orderRequestsRepository.findByLinkedinUrl(linkedinUrl).isPresent()) {
            throw new BadRequestException("Bu is ilanina bir kez basvurduk.");
        }
        OrderRequest order = new OrderRequest();
        order.setId(UUID.randomUUID().toString());
        order.setLinkedinUrl(linkedinUrl);
        order.setLocalDateTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setUserId(userId);
        return orderRequestsRepository.save(order);
    }

    @Override
    public QueueController.SentMailResponse seeSentMailByOrderId(String id) {
      SentMail sentMail = sentMailRepository.findSentMailByOrderId(id).orElseThrow(() -> new NotfoundException("sent mail not found."));
      LinkedInJobPostingResult linkedInJobPostingResult = linkedinJobPostingRepository.findById(sentMail.getJobPostingScraperResultId()).orElseThrow(()->new NotfoundException("Job posting result not found for sent mail."));
      LinkedInCompanyProfileResult linkedInCompanyProfileResult = linkedInCompanyProfileResultRepository.findById(sentMail.getCompanyScraperResultId()).orElseThrow(()->new NotfoundException("Company profile result not found for sent mail."));
      WebsiteScrapeResult websiteScrapeResult = websiteScrapeResultRepository.findById(sentMail.getWebsiteScraperResultId()).orElseThrow(()->new NotfoundException("Website scrape result not found for sent mail."));
      ChatGptResponse chatGptResponse = chatGptResponseRepository.findById(sentMail.getChatGptResponseId()).orElseThrow(()->new NotfoundException("Generated email result not found for sent mail."));
      return new QueueController.SentMailResponse(
              sentMail.getId(),
              sentMail.getSubject(),
              sentMail.getBody(),
              linkedInCompanyProfileResult,
              linkedInJobPostingResult,
              websiteScrapeResult,
              chatGptResponse,
              sentMail.getOrderId(),
              sentMail.getSentAt()
              );
    }

    @Override
    public void run(String orderId) throws IOException {
        OrderRequest order = orderRequestsRepository.findById(orderId).orElseThrow(() -> new NotfoundException("Order not found."));
        User user = userRepository.findById(order.getUserId()).orElseThrow(() -> new NotfoundException("User not found."));

        final int MAX_PAGES = 20;
        LinkedInJobPostingResult jobPostingResult = jobPostingService.scrape(order.getLinkedinUrl());
        LinkedInCompanyProfileResult companyProfileResult = companyProfileService.scrapeCompanyProfile(jobPostingResult.getCompanyProfileUrl());
        WebsiteScrapeResult websiteScrapeResult = websiteScraperService.scrape(companyProfileResult.getWebsite(), MAX_PAGES);

        if (websiteScrapeResult.getFoundEmails().isEmpty()) {
            throw new BadRequestException("Bir tane mail bile bulunamadi.");
        }
        System.out.println("islem bitti , bulunan mail adresleri:");
        for (String strina : websiteScrapeResult.getFoundEmails()) {
            System.out.println("[-]: " + strina);
        }

        System.out.println("simdi chatgptye soracagim");

        ChatGptResponse chatGptResponse = chatGPTService.generateJobApplicationEmail(jobPostingResult, user);
        System.out.println("chatgptden response geldi.");

        System.out.println("mailler yollaniliyor.");
        mailService.sendMultipartfileMailToCompanyMails(order, websiteScrapeResult.getFoundEmails().stream().toList(), chatGptResponse.getBody(), chatGptResponse.getSubject());


        SentMail sentMail = new SentMail();
        sentMail.setId(UUID.randomUUID().toString());
        sentMail.setSubject(chatGptResponse.getSubject());
        sentMail.setBody(chatGptResponse.getBody());
        sentMail.setJobPostingScraperResultId(jobPostingResult.getId());
        sentMail.setCompanyScraperResultId(companyProfileResult.getId());
        sentMail.setWebsiteScraperResultId(websiteScrapeResult.getId());
        sentMail.setChatGptResponseId(chatGptResponse.getId());
        sentMail.setSentAt(LocalDateTime.now());
        sentMail.setOrderId(order.getId());
        sentMailRepository.save(sentMail);
    }

    public void sendOrderDetailsToUser(OrderRequest request) throws MessagingException {
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new NotfoundException("User not found."));
        mailService.sendBasicMail(user.getEmail(), request.getLinkedinUrl() + " job posting is done. At:" + request.getLocalDateTime());

    }


    public void sendOrderFailedToUser(OrderRequest or, String body) throws MessagingException {
        User user = userRepository.findById(or.getUserId()).orElseThrow(() -> new NotfoundException("User not found."));
        mailService.sendBasicMail(user.getEmail(), body);
    }


    @Override
    public String allOrderRequestsLength() {
        return Integer.toString(orderRequestsRepository.findAllByStatus(OrderStatus.PENDING).size());
    }
}
