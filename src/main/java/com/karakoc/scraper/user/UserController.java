package com.karakoc.scraper.user;


import com.karakoc.scraper.exceptions.general.UnauthorizatedException;
import com.karakoc.scraper.orderrequests.OrderRequest;
import com.karakoc.scraper.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {


    private final UserService userService;

    public record PostCvSummaryRequest(String summary) {};
    public record SendAdditionalDetailsRequest(String fullname , String phoneNumber, String emailForMailSending, String address, String githubUrl, String linkedinUrl,String website){};

    @PostMapping("/cv-summary")
    public UserDTO setUserCvSummary(@AuthenticationPrincipal UserPrincipal principal, @RequestBody PostCvSummaryRequest r) {
        if (principal == null) {
            throw new UnauthorizatedException("Authentication required");
        }
        return userService.setUserCvSummary(principal.getUserId(), r.summary);
    }

    @GetMapping("/cv-summary")
    public ResponseEntity<UserManager.GetUserCvSummary> getUserCvSummary(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizatedException("Authentication required");
        }
        return userService.getUserCvSummary(principal.getUserId());
    }



    @PostMapping(value = "/cv-file", consumes = "multipart/form-data")
    public ResponseEntity<UserManager.SetUserCvFileResponse> setUserCvFile(@AuthenticationPrincipal UserPrincipal principal, @RequestParam("file") MultipartFile file) {
        if (principal == null) {
            throw new UnauthorizatedException("Authentication required");
        }
        return userService.setUserCvFile(principal.getUserId(), file);
    }

    @GetMapping("/cv-file")
    public ResponseEntity<UserManager.GetUserCvFileResponse> getUserCvFile(@AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            throw new UnauthorizatedException("Authentication required");
        }
        return userService.getUserCvFile(principal.getUserId());
    }

    @GetMapping("/orders")
    public List<OrderRequest> getOrders(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizatedException("Authentication required");
        }
        return userService.getAllOrdersByUserId(principal.getUserId());
    }

    @PostMapping("/send-additional-details")
    public UserDTO postAdditionalDetails(@AuthenticationPrincipal UserPrincipal principal, @RequestBody SendAdditionalDetailsRequest r){
        if (principal==null){
            throw new UnauthorizatedException("Unauthorized");
        }
        return userService.postAdditionalDetails(principal.getUserId(),r);
    }
}

