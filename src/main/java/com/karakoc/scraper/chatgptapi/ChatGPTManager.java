package com.karakoc.scraper.chatgptapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karakoc.scraper.exceptions.general.BadRequestException;
import com.karakoc.scraper.linkedinjobpostingscraper.LinkedInJobPostingResult;
import com.karakoc.scraper.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatGPTManager implements ChatGPTService {

    @Value("${chatgpt.api.url}")
    private String apiUrl;

    @Value("${chatgpt.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatGptResponseRepository repository;

    public ChatGPTManager(RestTemplateBuilder restTemplateBuilder, ChatGptResponseRepository repository) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.repository = repository;
    }

    @Override
    public ChatGptResponse generateJobApplicationEmail(LinkedInJobPostingResult jobPostingResult, User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Sistem mesajı: ChatGPT'ye profesyonel e-posta yazarı olarak davranıp, yanıtı JSON formatında üretmesi talimatı veriliyor.
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", """
                You are a professional email writer specialized in creating job application emails. Using the provided candidate JSON data and the job posting details (including roleTitle, roleDescription, company information, etc.), generate a formal and personalized job application email. The email must directly incorporate the actual details from both the candidate’s information and the job posting without any placeholders (for example, do not use terms like "[User's xxx field]" or "[Emirhan's Address]"). The email is intended to be sent directly to companies, so it must be error-free and fully complete.
                
                Instructions:
                
                Objective:
                Create a complete and ready-to-send job application email that directly references the provided candidate’s CV summary and the specific job posting details.
                
                Content Requirements:
                
                Identify the key qualifications and responsibilities mentioned in the job posting and align them with the candidate’s skills and experiences from the provided CV summary.
                
                Directly incorporate specific details from the job posting (such as the role title, company information, and role description) as well as explicit candidate information (like the candidate’s name, contact details, and relevant experiences) into the email content.
                
                Emphasize the candidate’s strengths based on the provided CV summary and mention a willingness to learn or quickly adapt in any areas that may not be a perfect match.
                
                Ensure that every detail is concrete and specific. There must be no generic placeholders or template text that require manual replacement.
                
                Signature Requirements:
                
                At the very end of the email, create a signature block that includes the candidate's fullname, emailForMailSending, address, githubUrl, linkedinUrl, and website. Do not include any additional information in the signature.
                
                Use the provided candidate fields for the signature. Do not use user.email; instead, use user.emailForMailSending.
                
                Output Format:
                
                The final response must be a valid JSON object with exactly two keys: "subject" and "body".
                
                The "subject" key should contain the email subject line.
                
                The "body" key should contain the full email text.
                
                Style and Tone:
                
                The email should be formal, well-structured, and grammatically correct.
                
                The tone must be professional and tailored to the specific job posting.
                
                The response must include only the actual details provided in the candidate’s data and the job description, with the specified signature information at the end of the email. No additional details should be appended.
                
                Ensure that your final output meets these requirements and is completely ready to be sent as an email without any further modifications. """);


        // Kullanıcı mesajı: İş ilanı açıklaması ve CV özetini içeriyor.
        StringBuilder userContent = new StringBuilder();
        userContent.append("Job Posting: ").append(jobPostingResult.toString()).append("\n\n");
        userContent.append("CV Summary: ").append(user.getCvSummary());
        userContent.append("User's fullname:").append(user.getFullname());
        userContent.append("User's phone number:").append(user.getPhoneNumber());
        userContent.append("User's email address:").append(user.getEmailForMailSending());
        userContent.append("User's address:").append(user.getAddress());
        userContent.append("User's github url:").append(user.getGithubUrl());
        userContent.append("User's linkedin url:").append(user.getLinkedinUrl());
        userContent.append("User's website:").append(user.getWebsite());



        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userContent.toString());

        // Mesaj listesini oluşturuyoruz
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);

        // API istek gövdesi
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List choices = (List) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map choice = (Map) choices.get(0);
                Map messageResponse = (Map) choice.get("message");
                if (messageResponse != null) {
                    // API'den dönen yanıtın "content" kısmı JSON string olarak geliyor
                    String content = (String) messageResponse.get("content");
                    try {
                        // ChatGptResponse entity'sine deserialize et
                        ChatGptResponse chatGptResponse = objectMapper.readValue(content, ChatGptResponse.class);
                        // Eğer id bilgisi yoksa, benzersiz id oluştur
                        if (chatGptResponse.getId() == null || chatGptResponse.getId().isEmpty()) {
                            chatGptResponse.setId(UUID.randomUUID().toString());
                        }
                        return repository.save(chatGptResponse);
                    } catch (Exception e) {
                        throw new BadRequestException("ChatGPT yanıtını entity'ye parse ederken hata oluştu.");
                    }
                }
            }
        }
        throw new BadRequestException("ChatGPT'den yanıt gelmedi. İşlem başarısız.");
    }
}
