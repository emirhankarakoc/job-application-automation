package com.karakoc.scraper.prodbykarakoc;

import com.karakoc.scraper.chatgptapi.ChatGptResponse;
import com.karakoc.scraper.exceptions.general.UnauthorizatedException;
import com.karakoc.scraper.linkedincompanyscraper.LinkedInCompanyProfileResult;
import com.karakoc.scraper.linkedinjobpostingscraper.LinkedInJobPostingResult;
import com.karakoc.scraper.orderrequests.OrderRequest;
import com.karakoc.scraper.orderrequests.OrderRequestsRepository;
import com.karakoc.scraper.security.UserPrincipal;
import com.karakoc.scraper.sentmails.SentMail;
import com.karakoc.scraper.sentmails.SentMailRepository;
import com.karakoc.scraper.websitescraper.WebsiteScrapeResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/queue")
@AllArgsConstructor
public class QueueController {

    private final QueueService queueService;
    private final SentMailRepository sentMailRepository;
    private final OrderRequestsRepository orderRequestsRepository;

    @PostMapping
    public OrderRequest post(@AuthenticationPrincipal UserPrincipal principal, @RequestParam String linkedinjobpostingUrl){
            return queueService.createOrder(principal.getUserId(),linkedinjobpostingUrl);
    }

    public record SentMailResponse(String id,
                                   String subject,
                                   String body,
                                   LinkedInCompanyProfileResult companyProfileResult,
                                   LinkedInJobPostingResult jobPostingResult,
                                   WebsiteScrapeResult websiteScrapeResult,
                                   ChatGptResponse chatGptResponse,
                                   String orderId,
                                   LocalDateTime sentAt){}

    @GetMapping("/sentmail-details/{orderId}")
    public QueueController.SentMailResponse seeSentMailByOrderId(@PathVariable String orderId){
        return queueService.seeSentMailByOrderId(orderId);
    }


    @GetMapping("/length")
    public String orderRequestsLength(@AuthenticationPrincipal UserPrincipal principal){
        if (principal == null) {
            throw new UnauthorizatedException("Login first.");
        }
        return queueService.allOrderRequestsLength();
    }

}
