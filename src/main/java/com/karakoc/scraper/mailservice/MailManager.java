package com.karakoc.scraper.mailservice;

import com.karakoc.scraper.cloudflare.R2Service;
import com.karakoc.scraper.exceptions.general.NotfoundException;
import com.karakoc.scraper.orderrequests.OrderRequest;
import com.karakoc.scraper.user.User;
import com.karakoc.scraper.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.UrlResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MailManager implements MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderAddress;

    private final UserRepository userRepository;
    private final R2Service r2Service;

    public MailManager(UserRepository userRepository, R2Service r2Service) {
        this.userRepository = userRepository;
        this.r2Service = r2Service;
    }

    /**
     * Sends multipart email with an optional CV attachment to companies and also to the requesting user.
     */
    public Map<String, Object> sendMultipartfileMailToCompanyMails(OrderRequest orderRequest, List<String> toList, String body, String subject) {
        String cvUrl = fetchUserCvUrl(orderRequest);
        User requestingUser = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new NotfoundException("User not found"));
        Map<String, Object> responses = new HashMap<>();

        // Send to company email addresses
        for (String to : toList) {
            try {
                MimeMessage mimeMessage = prepareEmailContent(to, body, subject, cvUrl, requestingUser.getCvFileName());
                mailSender.send(mimeMessage);
                responses.put(to, "Sent successfully.");
            } catch (MessagingException | IOException e) {
                log.error("Email sending error to: " + to, e);
                responses.put(to, "Error: " + e.getMessage());
            }
        }

        // Additionally, send the same email to the requesting user
        try {

            // Assuming User entity has a getEmail() method
            String userEmail = requestingUser.getEmail();
            MimeMessage userMimeMessage = prepareEmailContent(userEmail, body, subject, cvUrl, requestingUser.getCvFileName());
            mailSender.send(userMimeMessage);
            responses.put(userEmail, "Sent successfully.");
        } catch (MessagingException | IOException e) {
            log.error("Email sending error to requesting user: " + orderRequest.getUserId(), e);
            responses.put(orderRequest.getUserId(), "Error: " + e.getMessage());
        }

        return responses;
    }

    /**
     * Fetches user's CV public URL from R2 using the stored key.
     */
    private String fetchUserCvUrl(OrderRequest orderRequest) {
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new NotfoundException("User not found"));
        return r2Service.getPublicUrl(user.getCvUrlKey());
    }

    /**
     * Prepares email content with optional CV attachment.
     */
    private MimeMessage prepareEmailContent(String to, String body, String subject, String cvUrl,String cvFileName) throws MessagingException, IOException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

        helper.setFrom(senderAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        addCvAttachment(helper, cvUrl,cvFileName);
        return mimeMessage;
    }

    /**
     * Adds the user's CV from R2 as an attachment.
     */
    private void addCvAttachment(MimeMessageHelper helper, String cvUrl,String cvFileName) throws MessagingException, MalformedURLException {
        try {
            URL url = new URL(cvUrl);
            InputStreamSource cvFile = new UrlResource(url);
            helper.addAttachment(cvFileName, cvFile);
        } catch (IOException e) {
            log.error("Error attaching CV from R2: " + cvUrl, e);
            throw new MessagingException("Failed to attach CV file.");
        }
    }

    /**
     * Sends a basic email with plain text.
     */
    @Override
    public void sendBasicMail(String to, String body) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        helper.setFrom(senderAddress);
        helper.setTo(to);
        helper.setSubject("New Mail From App!");
        helper.setText(body);

        mailSender.send(mimeMessage);
    }
}
